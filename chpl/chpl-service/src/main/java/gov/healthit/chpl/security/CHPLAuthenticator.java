package gov.healthit.chpl.security;

import org.springframework.beans.factory.annotation.Autowired;

import gov.healthit.chpl.auth.authentication.UserAuthenticator;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.jwt.JWTAuthor;
import gov.healthit.chpl.auth.jwt.JWTCreationException;
import gov.healthit.chpl.auth.manager.UserManager;



public class CHPLAuthenticator extends UserAuthenticator {

	@Autowired
	private JWTAuthor jwtAuthor;
	
	@Autowired
	private UserManager userManager;
	
	@Override
	public String getJWT(UserDTO user) throws JWTCreationException {
		
		String jwt = null;
		return jwt;
		
	}
	
}