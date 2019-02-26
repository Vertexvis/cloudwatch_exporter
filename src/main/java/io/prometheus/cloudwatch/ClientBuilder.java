package io.prometheus.cloudwatch;
import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.client.builder.AwsSyncClientBuilder;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.elasticloadbalancingv2.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancingv2.AmazonElasticLoadBalancingClientBuilder;
import java.util.Map;

public class ClientBuilder {

    private Map<String, Object> config;
    private AWSCredentialsProvider credentialsProvider;

    public ClientBuilder(Map<String, Object> config) {
        this.config = config;
        this.credentialsProvider = this.getCredentialProvider();
    }

    public AmazonCloudWatchClient getCloudwatchClient() {
        return (AmazonCloudWatchClient)buildClient(AmazonCloudWatchClientBuilder.standard());
    }

    public AmazonElasticLoadBalancing getLoadBalancingClient() {
        return (AmazonElasticLoadBalancing)buildClient(AmazonElasticLoadBalancingClientBuilder.standard());
    }

    private AmazonWebServiceClient buildClient(AwsSyncClientBuilder builder) {
        if (this.credentialsProvider != null) {
            builder.withCredentials(this.credentialsProvider);
        }
        builder.withRegion((String)config.get("region"));
        return (AmazonWebServiceClient)builder.build();
    }

    private AWSCredentialsProvider getCredentialProvider() {
        if (config.containsKey("role_arn")) {
            return new STSAssumeRoleSessionCredentialsProvider.Builder(
                    (String)config.get("role_arn"), "cloudwatch_exporter").build();
        }
        return null;
    }
}
