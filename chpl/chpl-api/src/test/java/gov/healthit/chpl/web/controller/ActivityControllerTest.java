package gov.healthit.chpl.web.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.domain.ActivityEvent;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml") 
public class ActivityControllerTest {
	private static JWTAuthenticatedUser adminUser;

	@Autowired Environment env;
	@Autowired ActivityController activityController;
	
	@Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		adminUser = new JWTAuthenticatedUser();
		adminUser.setFirstName("Administrator");
		adminUser.setId(-2L);
		adminUser.setLastName("Administrator");
		adminUser.setSubjectName("admin");
		adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
	}
	
	/** 
	 * Tests that listActivity returns results for a Certified Product
	 * @throws IOException 
	 * @throws ValidationException 
	 */
	@Transactional
	@Test
	public void test_listActivity() throws EntityRetrievalException, EntityCreationException, IOException, ValidationException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		// Note: certification_criterion_id="59" has testTool="true", number 170.315 (h)(1) and title "Direct Project"
		Long cpId = 1L; // this CP has ics_code = "1" & associated certification_result_id = 8 with certification_criterion_id="59"
		Long cpId2 = 10L; // this CP has ics_code = "0" & associated certification_result_id = 9 with certification_criterion_id="59"
		Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		start.set(2015, 9, 1, 0, 0);
		Calendar end = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        end.set(2015, 9, 20, 0, 0);
        
		List<ActivityEvent> cpActivityEvents = activityController.
		        activityForCertifiedProductById(cpId);
		assertEquals(3, cpActivityEvents.size());
		
		List<ActivityEvent> cp2ActivityEvents = activityController.
		        activityForCertifiedProductById(cpId2);
		assertEquals(0, cp2ActivityEvents.size());
	}
	
	@Transactional
    @Test(expected=IllegalArgumentException.class)
    public void testStartDateRequired() throws EntityRetrievalException, EntityCreationException, IOException, ValidationException{
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        activityController.activityForACBs(System.currentTimeMillis(), null, false);
    }
	
	@Transactional
    @Test(expected=IllegalArgumentException.class)
    public void testEndDateRequired() throws EntityRetrievalException, EntityCreationException, IOException, ValidationException{
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        activityController.activityForACBs(null, System.currentTimeMillis(), false);
    }
	
	@Transactional
    @Test(expected=IllegalArgumentException.class)
    public void testStartAndEndDateRequired() throws EntityRetrievalException, EntityCreationException, IOException, ValidationException{
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        activityController.activityForACBs(null, null, false);
    }
	
	/** 
	 * GIVEN a user is looking at activity
	 * WHEN they try to search for more than configurable days (set to 60)
	 * THEN they should not be able to make the call
	 * @throws IOException 
	 * @throws ValidationException 
	 */
	@Transactional
	@Test(expected=IllegalArgumentException.class)
	public void test_dateValidation_outOfRangeThrowsException() throws EntityRetrievalException, EntityCreationException, IOException, ValidationException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		activityController.activityForACBs(0L, cal.getTimeInMillis(), false);
	}
	
	/** 
	 * GIVEN a user is looking at activity
	 * WHEN they try to search for a date range within the configurable days (set to 60)
	 * THEN they should be able to make the call
	 * @throws IOException 
	 * @throws ValidationException 
	 */
	@Transactional
	@Test
	public void test_dateValidation_insideRangeDoesNotThrowException() throws EntityRetrievalException, EntityCreationException, IOException, ValidationException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Calendar calEnd = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		Calendar calStart = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		Integer maxActivityRangeInDays = Integer.getInteger(
		        env.getProperty("maxActivityRangeInDays"), ActivityController.DEFAULT_MAX_ACTIVITY_RANGE_DAYS);
		calStart.add(Calendar.DATE, -maxActivityRangeInDays + 1);
		activityController.activityForACBs(calStart.getTimeInMillis(), calEnd.getTimeInMillis(), false);
	}
	
	/** 
	 * GIVEN a user is looking at activity
	 * WHEN they try to search for a date range equal to the max number of days (set to 60)
	 * THEN they should be able to make the call
	 * @throws IOException 
	 * @throws ValidationException 
	 */
	@Transactional
	@Test
	public void test_dateValidation_allowsMaxActivityRangeDays() throws EntityRetrievalException, EntityCreationException, IOException, ValidationException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Calendar calEnd = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		Calendar calStart = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		Integer maxActivityRangeInDays = Integer.getInteger(
		        env.getProperty("maxActivityRangeInDays"), ActivityController.DEFAULT_MAX_ACTIVITY_RANGE_DAYS);
		calStart.add(Calendar.DATE, -maxActivityRangeInDays);
		activityController.activityForACBs(calStart.getTimeInMillis(), calEnd.getTimeInMillis(), false);
	}
	
	/** 
	 * GIVEN a user is looking at activity
	 * WHEN they try to search for a date range greater than the max number of days (set to 60)
	 * THEN they should not be able to make the call
	 * @throws IOException 
	 * @throws ValidationException 
	 */
	@Transactional
	@Test(expected=IllegalArgumentException.class)
	public void test_dateValidation_ExceptionForMaxDateRangePlusOneDays() throws EntityRetrievalException, EntityCreationException, IOException, ValidationException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Calendar calEnd = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		Calendar calStart = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		calStart.add(Calendar.DATE, (ActivityController.DEFAULT_MAX_ACTIVITY_RANGE_DAYS+1)*-1);
		activityController.activityForACBs(calStart.getTimeInMillis(), calEnd.getTimeInMillis(), false);
	}
	
	/** 
	 * GIVEN a user is looking at activity
	 * WHEN they try to search for a date range with the start date > end date
	 * THEN they should not be able to make the call
	 * @throws IOException 
	 * @throws ValidationException 
	 */
	@Transactional
	@Test(expected=IllegalArgumentException.class)
	public void test_dateValidation_startDateAfterEndDate() throws EntityRetrievalException, EntityCreationException, IOException, ValidationException{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Calendar calEnd = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		Calendar calStart = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		calStart.add(Calendar.DATE, 20);
		activityController.activityForACBs(calStart.getTimeInMillis(), calEnd.getTimeInMillis(), false);
	}
	
	@Transactional
	@Test(expected=EntityRetrievalException.class)
	public void testGetAcbActivityWithBadId() 
		throws EntityRetrievalException, IOException, ValidationException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        start.set(2015, 9, 1, 0, 0);
        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        end.set(2015, 9, 20, 0, 0);
		activityController.activityForACBById(-100L, false);
	}
	
	@Transactional
	@Test(expected=EntityRetrievalException.class)
	public void testGetAnnouncementActivityWithBadId() 
		throws EntityRetrievalException, IOException, ValidationException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        start.set(2015, 9, 1, 0, 0);
        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        end.set(2015, 9, 20, 0, 0);
		activityController.activityForAnnouncementById(-100L);
	}
	
	@Transactional
	@Test(expected=EntityRetrievalException.class)
	public void testGetAtlActivityWithBadId() 
		throws EntityRetrievalException, IOException, ValidationException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        start.set(2015, 9, 1, 0, 0);
        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        end.set(2015, 9, 20, 0, 0);
		activityController.activityForATLById(-100L, false);
	}
	
	@Transactional
	@Test(expected=EntityRetrievalException.class)
	public void testGetCertificationActivityWithBadId() 
		throws EntityRetrievalException, IOException, ValidationException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        start.set(2015, 9, 1, 0, 0);
        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        end.set(2015, 9, 20, 0, 0);
		activityController.activityForCertificationById(-100L);
	}
	
	@Transactional
	@Test(expected=EntityRetrievalException.class)
	public void testGetListingActivityWithBadId() 
		throws EntityRetrievalException, IOException, ValidationException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        start.set(2015, 9, 1, 0, 0);
        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        end.set(2015, 9, 20, 0, 0);
		activityController.activityForCertifiedProductById(-100L);
	}
	
	@Transactional
	@Test(expected=EntityRetrievalException.class)
	public void testGetDeveloperActivityWithBadId() 
		throws EntityRetrievalException, IOException, ValidationException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        start.set(2015, 9, 1, 0, 0);
        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        end.set(2015, 9, 20, 0, 0);
		activityController.activityForDeveloperById(-100L);
	}
	
	@Transactional
	@Test(expected=EntityRetrievalException.class)
	public void testGetPendingListingActivityWithBadId() 
		throws EntityRetrievalException, IOException, ValidationException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        start.set(2015, 9, 1, 0, 0);
        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        end.set(2015, 9, 20, 0, 0);
		activityController.activityForPendingCertifiedProductById(-100L);
	}
	
	@Transactional
	@Test(expected=EntityRetrievalException.class)
	public void testGetProductActivityWithBadId() 
		throws EntityRetrievalException, IOException, ValidationException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        start.set(2015, 9, 1, 0, 0);
        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        end.set(2015, 9, 20, 0, 0);
		activityController.activityForProducts(start.getTimeInMillis(), end.getTimeInMillis());
	}
	
	@Transactional
	@Test(expected=UserRetrievalException.class)
	public void testGetUserActivityWithBadId() 
		throws UserRetrievalException, IOException, ValidationException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        start.set(2015, 9, 1, 0, 0);
        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        end.set(2015, 9, 20, 0, 0);
		activityController.activityForUsers(start.getTimeInMillis(), end.getTimeInMillis());
	}
	
	@Transactional
	@Test(expected=UserRetrievalException.class)
	public void testGetUserActivitesWithBadId() 
		throws UserRetrievalException, IOException, ValidationException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        start.set(2015, 9, 1, 0, 0);
        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        end.set(2015, 9, 20, 0, 0);
		activityController.activityByUser(-100L);
	}
	
	@Transactional
	@Test(expected=EntityRetrievalException.class)
	public void testGetVersionActivityWithBadId() 
		throws EntityRetrievalException, IOException, ValidationException {
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        start.set(2015, 9, 1, 0, 0);
        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        end.set(2015, 9, 20, 0, 0);
		activityController.activityForVersions(start.getTimeInMillis(), end.getTimeInMillis());
	}
	
	@Test
	public void testDateRangeComparisonWorksAcrossDaylightSavings() {
	    //two dates where daylight savings falls between them
	    Long startDate = 1505620800000L; //9/17/17
	    Long endDate = 1510808400000L; //11/16/17
	    
	    LocalDate startDateUtc = 
	            Instant.ofEpochMilli(startDate).atZone(ZoneId.of("UTC")).toLocalDate();
	    LocalDate endDateUtc = 
                Instant.ofEpochMilli(endDate).atZone(ZoneId.of("UTC")).toLocalDate();
	    
        System.out.println("Start: " + startDateUtc);
        System.out.println("End: " + endDateUtc);
        if(startDateUtc.isAfter(endDateUtc)) {
            fail("End date is not before start date.");
        }

        Integer maxActivityRangeInDays = Integer.getInteger(env.getProperty("maxActivityRangeInDays"), 60);
        
        endDateUtc = endDateUtc.minusDays(maxActivityRangeInDays);
        if(startDateUtc.isBefore(endDateUtc)) {
            System.out.println("Start: " + startDateUtc);
            System.out.println("End minus " + maxActivityRangeInDays + " days: " + endDateUtc);
            fail("Date range is not more than 60 days apart");
        }
        
        System.out.println("End minus " + maxActivityRangeInDays + " days: " + endDateUtc);
	}
}
