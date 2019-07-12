kubectl apply -f zookeeper/service.yml \
-f zookeeper/pod_disruption_budget.yml \
-f zookeeper/statefulset.yml
