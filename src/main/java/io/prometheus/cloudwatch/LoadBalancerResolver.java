package io.prometheus.cloudwatch;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.elasticloadbalancingv2.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancingv2.AmazonElasticLoadBalancingClientBuilder;
import com.amazonaws.services.elasticloadbalancingv2.model.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class LoadBalancerResolver implements Resolver {
    private static final Logger LOGGER = Logger.getLogger(LoadBalancerResolver.class.getName());
    private AmazonElasticLoadBalancingClientBuilder builder = AmazonElasticLoadBalancingClientBuilder.standard();
    private AmazonElasticLoadBalancing client;
    private Map<String, String> loadBalancerDetails = new HashMap<>();

    LoadBalancerResolver() {
        this.client = this.builder.build();
    }

    LoadBalancerResolver(AWSCredentialsProvider credentialsProvider) {
        this.client = this.builder.withCredentials(credentialsProvider).build();
    }

    @Override
    public String resolve(String resource) {
        if (!loadBalancerDetails.containsKey(resource)) {
            refreshLatestLoadBalancers();
        }
        return loadBalancerDetails.getOrDefault(resource, "NA");
    }

    private void refreshLatestLoadBalancers() {
        DescribeLoadBalancersRequest lbRequest = new DescribeLoadBalancersRequest();
        LOGGER.fine(lbRequest.toString());

        DescribeLoadBalancersResult describeLoadBalancersResult = client.describeLoadBalancers(lbRequest);
        List<LoadBalancer> loadBalancers = describeLoadBalancersResult.getLoadBalancers();
        List<String> lbArns = loadBalancers.stream()
                .map(LoadBalancer::getLoadBalancerArn)
                .collect(Collectors.toList());

        DescribeTagsRequest tagRequest = new DescribeTagsRequest();
        tagRequest.setResourceArns(lbArns);

        LOGGER.fine(tagRequest.toString());

        DescribeTagsResult tags = client.describeTags(tagRequest);
        List<TagDescription> tagDescriptions = tags.getTagDescriptions();

        for (LoadBalancer loadBalancer : loadBalancers) {
            for (TagDescription tagDescription : tagDescriptions) {
                if (loadBalancer.getLoadBalancerArn().equals(tagDescription.getResourceArn())) {
                    String loadBalancerName = loadBalancer.getLoadBalancerName();
                    Tag projectTag = tagDescription.getTags().stream()
                            .filter(tag -> "Project".equals(tag.getKey()))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException(
                                    "Could not find project tag for lb: " + loadBalancerName));

                    String tagValue = projectTag.getValue();
                    LOGGER.info("loaded up " + loadBalancerName + " : " + tagValue);
                    loadBalancerDetails.put(loadBalancerName, tagValue);
                }
            }
        }
    }
}
