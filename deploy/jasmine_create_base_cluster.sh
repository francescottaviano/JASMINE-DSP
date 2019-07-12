cd "$(dirname "$0")"

export KOPS_STATE_STORE=s3://jasmine-kops-store
./jasmine_cluster.sh create --public-key /Users/simone/Documents/AWS/pem/jasmine-kops-cluster/jasmine.pub
kops update cluster simonemancini.eu --yes
