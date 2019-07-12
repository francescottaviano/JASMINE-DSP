
# general
REGION=eu-west-1
AVAILABILITY_ZONE=${REGION}a

# kubernetes cluster
NAME=simonemancini.eu
KOPS_STATE_STORE=s3://jasmine-kops-store

# EBS
VOLUME_SIZE=1
VOLUME_TYPE=gp2
VOLUME_ID=""

#kay pair
PUBLIC_KEY=""

#master size and count
MASTER_SIZE="t2.micro"
MASTER_COUNT=1

#node size and count
NODE_SIZE="m4.large"
NODE_COUNT=3

# commands
CREATE_COMMAND=create
DELETE_COMMAND=delete
ADDONS_COMMAND=addons

#kops commands
KOPS_UPDATE_CLUSTER_COMMAND="kops update cluster $NAME --yes"

#cluster name
CLUSTER_NAME=""

# misc
USAGE_MESS=$(cat <<-EOF
  Usage: jasmine_cluster.sh <create | delete | addons >

  Options:
    --public-key [path to .pub file] : specify .pub path when create operation called;
                      NECESSARY with 'create' command

    --volume-id [ID] : specify EBS volume ID when delete operation called;
                      NECESSARY with 'delete' command

    --cluster [cluster name] : select cluster to keep up, they could be defined multiple time;
                      NECESSARY with 'addons' command
                      LIST [kafka, zookeeper]

    --stateful-set [cluster name] : create StatefulSet from template and configuration files;
                      NECESSARY with 'addons' command
                      LIST [kafka, zookeeper]
EOF
)

if [ "$1" == "" ]; then
  echo "$USAGE_MESS"
  exit 1
fi



if [ "$1" == $CREATE_COMMAND ]; then

  shift
  if [ "$1" == "" ]; then
    echo "$USAGE_MESS"
    exit 1
  fi

  while [ "$1" != "" ]; do
    case $1 in
      --public-key )  shift
                    PUBLIC_KEY=$1;;
      * )           echo "$USAGE_MESS"
                    exit 1
    esac
    shift
  done

  echo 'Creating AWS S3 Bucket '
  echo $KOPS_STATE_STORE
  aws s3 mb $KOPS_STATE_STORE --region ${REGION}

  if [ $? -ne 0 ]; then
    echo "S3 Bucket creation ERROR"
    exit 1
  fi

  echo 'Creating AWS EBS Volume'
  VOLUME_ID=$(aws ec2 create-volume \
      --size $VOLUME_SIZE \
      --volume-type $VOLUME_TYPE \
      --region $REGION \
      --availability-zone $AVAILABILITY_ZONE \
      | python3 -c "import sys, json; print(json.load(sys.stdin)['VolumeId'])")


  if [ ${PIPESTATUS[0]} -ne 0 ]; then
    echo "EBS Volume Creation ERROR"
    exit 1
  fi

  echo "Created EBS Volume with ID: $VOLUME_ID "

  echo 'Creating AWS Cluster'
  kops create cluster \
      --topology public \
      --zones $AVAILABILITY_ZONE \
      --master-zones $AVAILABILITY_ZONE \
      --state $KOPS_STATE_STORE \
      --master-count $MASTER_COUNT \
      --master-size $MASTER_SIZE \
      --node-count $NODE_COUNT \
      --node-size $NODE_SIZE \
      --cloud aws \
      --ssh-public-key $PUBLIC_KEY \
      ${NAME}


  if [ $? -ne 0 ]; then
    echo "KOPS cluster creation Error"
    exit 1
  fi

  echo "Cluster created"

fi # create end

if [ "$1" == $DELETE_COMMAND ]; then

  shift
  if [ "$1" == "" ]; then
    echo "$USAGE_MESS"
    exit 1
  fi

  while [ "$1" != "" ]; do
    case $1 in
      --volume-id )  shift
                    VOLUME_ID=$1;;
      * )           echo "$USAGE_MESS"
                    exit 1
    esac
    shift
  done

  echo "Deleting kops cluster : $NAME"
  kops delete cluster --name ${NAME} --yes

  echo "Deleting EBS Volume with ID : $VOLUME_ID"
  aws ec2 delete-volume --volume-id $VOLUME_ID

  if [ $? -eq 0 ]; then
    echo "EBS Volume with ID $VOLUME_ID deleted successfully"
  fi

  echo "Deleting S3 Bucket"
  aws s3 rb $KOPS_STATE_STORE

  if [ $? -eq 0 ]; then
    echo "S3 bucket $KOPS_STATE_STORE removed successfully"
  fi


fi # delete end

if [ "$1" == $ADDONS_COMMAND ]; then

  shift
  if [ "$1" == "" ]; then
    echo "$USAGE_MESS"
    exit 1
  fi

  while [ "$1" != "" ]; do
    case $1 in
            --cluster )   shift
                          CLUSTER_NAME=$1
                          case $CLUSTER_NAME in
                            kafka )       echo "Creating Kafka cluster"
                                          kops toolbox template \
                                          --template templates/jasmine_cluster_template.yml \
                                          --values clusters/config/jasmine_kafka_cluster_config.yml \
                                          --output clusters/jasmine_kafka_cluster.yml \
                                          --format-yaml
                                          kops create -f clusters/jasmine_kafka_cluster.yml
                                          echo "Kafka cluster information stored in $KOPS_STATE_STORE";;
                            zookeeper )   echo "Creating Zookeeper cluster"
                                          kops toolbox template \
                                          --template templates/jasmine_cluster_template.yml \
                                          --values clusters/config/jasmine_zookeeper_cluster_config.yml \
                                          --output clusters/jasmine_zookeeper_cluster.yml \
                                          --format-yaml
                                          kops create -f clusters/jasmine_zookeeper_cluster.yml
                                          echo "Zookeeper cluster information stored in $KOPS_STATE_STORE";;
                            * )           echo "$USAGE_MESS"
                                          exit 1
                          esac;;
      --stateful-set )    shift
                          CLUSTER_NAME=$1
                          case $CLUSTER_NAME in
                            kafka )       echo "Creating Kafka StatefulSet"
                                          chmod 744 containers/kafka/jasmine_kafka_stateful_set.sh
                                          ./containers/kafka/jasmine_kafka_stateful_set.sh
                                          echo "Kafka StatefulSet created";;
                            zookeeper )   echo "Creating Zookeeper StatefulSet"
                                          chmod 744 containers/zookeeper/jasmine_zookeeper_stateful_set.sh
                                          ./containers/zookeeper/jasmine_zookeeper_stateful_set.sh
                                          echo "Zookeeper StatefulSet created";;
                            * )           echo "$USAGE_MESS"
                                          exit 1
                          esac;;
      * )           echo "$USAGE_MESS"
                    exit 1
    esac
    shift
  done

  echo "Cluster information updated successfully, to complete cluster updating run: $KOPS_UPDATE_CLUSTER_COMMAND"

fi #addons end
