kubectl apply -f kafka/service.yml \
-f kafka/pod_disruption_budget.yml \
-f kafka/statefulset.yml
