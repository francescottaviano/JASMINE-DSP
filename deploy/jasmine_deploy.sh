cd "$(dirname "$0")"

function yes_no {

  sleep 5s

  kubectl get pods

  # print question and read answer
  read -p "Could I go on? (Y/N)" answer

  # all to lower case
  answer=$(echo $answer | awk '{print tolower($0)}')

  # check and act on given answer
  case $answer in
    "y")    echo $1
            $2 ;;
    "n")    yes_no "$1" $2 ;;
    *)      echo "Please answer y or n" ; yes_no "$1" $2 ;;
  esac
}

chmod 744 jasmine_deploy_first.sh jasmine_deploy_second.sh jasmine_deploy_third.sh

echo "Start to deploy firt part of JASMINE cluster..."

./jasmine_deploy_first.sh

echo "First part deployed!"

yes_no "Start to deploy second part of JASMINE cluster..." ./jasmine_deploy_second.sh

echo "Second part deployed!"

yes_no "Start to deploy third part of JASMINE cluster..." ./jasmine_deploy_third.sh

kubectl get pods

echo "Third part deployed!"
