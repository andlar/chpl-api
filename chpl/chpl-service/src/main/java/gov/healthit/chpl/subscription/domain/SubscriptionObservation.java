package gov.healthit.chpl.subscription.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionObservation {

    private Long id;
    private Subscription subscription;
    private Long activityId;
}
