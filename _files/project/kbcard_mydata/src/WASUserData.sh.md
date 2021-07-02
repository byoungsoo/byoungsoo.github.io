```bash
#!/bin/bash
#Rsync /fscm/sync to /fsapp
output : {all : '|  tee -a /var/log/cloud-init-output.log'}
exec > >(tee /var/log/user-data.log) 2>&1
execute_service(){
 #Was start
  echo "Starting all service(WAS)"
  sudo su - wasadm -c "cd /fswas/domains/uaadomain; ./startuaaAdmin.sh" > /dev/null 2>&1
  sleep 20
  sudo su - wasadm -c "cd /fswas/domains/uaadomain; ./startkatsv301_i01.sh" > /dev/null 2>&1
  sleep 5
  sudo su - wasadm -c "cd /fswas/domains/uaadomain; ./startuaasv01_i01.sh" > /dev/null 2>&1
  sleep 5
 #TOSS Service
  /usr/local/secuve/TOSSuite/bin/ScvWatch &
  /usr/local/secuve/TOSSuite/bin/scvagt.sh start
  sleep 2
  service awslogs restart
  sleep 2
}
sync_to_fsapp(){
  RSYNC=`which rsync`
  $RSYNC -avz --delete --progress /fscm/sync/ /fsapp
  sleep 3
}
#ITSM WAS REGISTER
#CODE="was COD_00791"
#ITSM_ROOT_DIR="/fsutil/itsm"
#ITSM_REGI(){
#  cd $ITSM_ROOT_DIR
#  sh itsm-prd.sh $CODE >> /tmp/regi_was_$(date +%Y-%m-%d).log
#}
#Basic C zone tag name
CZNAME="SEOUL-LiivMate-C-PRD-WAS-EC2"
AZNAME="SEOUL-LiivMate-A-PRD-WAS-EC2"
#For routing 10.95.248.x
IFCONFIG_248(){
	 if [[ ! $R ]]
	 then
	   echo "No $R variables!"
	   exit 1
	 fi
	/sbin/ifconfig eth0 down
	/sbin/ip rule add from 10.95.248.192/27 lookup eth1_rt prio 1000
	/sbin/ifconfig eth1 $R netmask 255.255.255.224 up
	/sbin/ip route add default via 10.95.248.193 dev eth1 table eth1_rt
	/sbin/route add default gw 10.95.248.193 eth1
	/sbin/ifconfig eth0 up
	#Change sshd config from password no to yes
	/bin/sed -i '65s/no/yes/g' /etc/ssh/sshd_config
	/bin/systemctl restart sshd
	sleep 10
	/sbin/route add -net 10.95.250.128/26 gw 10.95.248.129 metric 0 dev eth0
	/bin/sed -i '21s/10\.210\.32\.42/10\.210\.32\.41/g' /etc/hosts
	/bin/sed -i '22s/10\.210\.32\.41/10\.210\.32\.42/g' /etc/hosts
}
#For routing 10.95.250.x
IFCONFIG_250(){
	 if [[ ! $R ]]
	 then
	   echo "No $R variables!"
	   exit 1
	 fi
	/sbin/ifconfig eth0 down
	/sbin/ip rule add from 10.95.250.192/27 lookup eth1_rt prio 1000
	/sbin/ifconfig eth1 $R netmask 255.255.255.224 up
	/sbin/ip route add default via 10.95.250.193 dev eth1 table eth1_rt
	/sbin/route add default gw 10.95.250.193 eth1
	/sbin/ifconfig eth0 up
	#Change sshd config from password no to yes
	/bin/sed -i '65s/no/yes/g' /etc/ssh/sshd_config
	/bin/systemctl restart sshd
	sleep 10
	/sbin/route add -net 10.95.248.128/26 gw 10.95.250.129 metric 0 dev eth0
	/bin/sed -i '21s/10\.210\.32\.41/10\.210\.32\.42/g' /etc/hosts
	/bin/sed -i '22s/10\.210\.32\.42/10\.210\.32\.41/g' /etc/hosts
}
CHK_PRESUB=$(netstat -r | tail -1 | awk '{print $1}' | awk -F'.' '{print $1$2$3}')
INSTANCE_ID=$(curl -s http://169.254.169.254/latest/meta-data/instance-id)
##echo $CHK_PRESUB
if [[ $CHK_PRESUB == "1095248" ]]
then
 HUBO_248_0="10.95.248.200"
 HUBO_248_0_ENI="eni-0346abd8a4d3265fc"
 HUBO_248_1="10.95.248.201"
 HUBO_248_1_ENI="eni-0e724dab47593e6bd"
 HUBO_248_2="10.95.248.202"
 HUBO_248_2_ENI="eni-0ced4540370e73433"
 HUBO_248_3="10.95.248.203"
 HUBO_248_3_ENI="eni-0fd71734147b0e9af"
 HUBO_248_4="10.95.248.204"
 HUBO_248_4_ENI="eni-0a2bc77db206511d9"
#
 arr=($HUBO_248_0 $HUBO_248_1 $HUBO_248_2 $HUBO_248_3 $HUBO_248_4)
#
# #echo ${#arr[@]}
 for ip in $(seq 0 ${#arr[@]})
 do
  INUSE=$(/usr/local/bin/aws ec2 describe-network-interfaces --filters Name=addresses.private-ip-address,Values="${arr[ip]}" --region ap-northeast-2 | grep -i status | tail -1 | awk -F':' '{print $2}' | sed 's/\,//g' | sed 's/^ //g')
  echo $INUSE
  #echo ${arr[$ip]}
   #ping -c 3 ${arr[$ip]}
   if [[ $INUSE != "\"in-use\"" ]]
   then
        R=${arr[$ip]}
        echo $R
        if [[ $R == "$HUBO_248_0" ]]
        then
          RENI=$HUBO_248_0_ENI
          /usr/local/bin/aws ec2 attach-network-interface --network-interface-id $RENI --instance-id $INSTANCE_ID --device-index 1 --region ap-northeast-2
          sleep 3
          IFCONFIG_248
          sync_to_fsapp
          #set hostname
          HNAME="ppmdapo1"
          #echo "$R $HNAME" >> /etc/hosts
          /usr/bin/hostnamectl set-hostname $HNAME
          #Change ec2 tag Name
          TAGNAME="$CZNAME-$HNAME"
          /usr/local/bin/aws ec2 create-tags --resources $INSTANCE_ID --tags 'Key="Name",Value="'"$TAGNAME"'"' --region ap-northeast-2
          sleep 9
          /bin/sed -i 's/PRD\-LiivMate\-ppmdapo2\-Was/PRD\-LiivMate\-ppmdapo1\-Was/g' /var/awslogs/etc/awslogs.conf
          sleep 2
          #ITSM_REGI
          execute_service
          #run this instance in only WAS 1,2
          sudo su - wasadm -c "cd /fswas/domains/uaadomain; ./startkatad301_i01.sh" > /dev/null 2>&1
          sleep 5
          exit 0
        elif [[ $R == "$HUBO_248_1" ]]
        then
          RENI=$HUBO_248_1_ENI
          /usr/local/bin/aws ec2 attach-network-interface --network-interface-id $RENI --instance-id $INSTANCE_ID --device-index 1 --region ap-northeast-2
          sleep 3
          IFCONFIG_248
          sync_to_fsapp
          #set hostname
          HNAME="ppmdapo3"
          #echo "$R $HNAME" >> /etc/hosts
          /usr/bin/hostnamectl set-hostname $HNAME
          #Change ec2 tag Name
          TAGNAME="$CZNAME-$HNAME"
          /usr/local/bin/aws ec2 create-tags --resources $INSTANCE_ID --tags 'Key="Name",Value="'"$TAGNAME"'"' --region ap-northeast-2
          sleep 9
          /bin/sed -i 's/PRD\-LiivMate\-ppmdapo2\-Was/PRD\-LiivMate\-ppmdapo3\-Was/g' /var/awslogs/etc/awslogs.conf
          sleep 2
          #ITSM_REGI
          execute_service
          exit 0
        elif [[ $R == "$HUBO_248_2" ]]
        then
          RENI=$HUBO_248_2_ENI
          /usr/local/bin/aws ec2 attach-network-interface --network-interface-id $RENI --instance-id $INSTANCE_ID --device-index 1 --region ap-northeast-2
          sleep 3
          IFCONFIG_248
          sync_to_fsapp
          #set hostname
          HNAME="ppmdapo5"
          #echo "$R $HNAME" >> /etc/hosts
          /usr/bin/hostnamectl set-hostname $HNAME
          #Change ec2 tag Name
          TAGNAME="$CZNAME-$HNAME"
          /usr/local/bin/aws ec2 create-tags --resources $INSTANCE_ID --tags 'Key="Name",Value="'"$TAGNAME"'"' --region ap-northeast-2
          sleep 9
          /bin/sed -i 's/PRD\-LiivMate\-ppmdapo2\-Was/PRD\-LiivMate\-ppmdapo5\-Was/g' /var/awslogs/etc/awslogs.conf
          sleep 2
          #ITSM_REGI
          execute_service
          exit 0
        elif [[ $R == "$HUBO_248_3" ]]
        then
          RENI=$HUBO_248_3_ENI
          /usr/local/bin/aws ec2 attach-network-interface --network-interface-id $RENI --instance-id $INSTANCE_ID --device-index 1 --region ap-northeast-2
          sleep 3
          IFCONFIG_248
          sync_to_fsapp
          #set hostname
          HNAME="ppmdapo7"
          #echo "$R $HNAME" >> /etc/hosts
          /usr/bin/hostnamectl set-hostname $HNAME
          #Change ec2 tag Name
          TAGNAME="$CZNAME-$HNAME"
          /usr/local/bin/aws ec2 create-tags --resources $INSTANCE_ID --tags 'Key="Name",Value="'"$TAGNAME"'"' --region ap-northeast-2
          sleep 9
          /bin/sed -i 's/PRD\-LiivMate\-ppmdapo2\-Was/PRD\-LiivMate\-ppmdapo7\-Was/g' /var/awslogs/etc/awslogs.conf
          sleep 2
          #ITSM_REGI
          execute_service
          exit 0
        elif [[ $R == "$HUBO_248_4" ]]
        then
          RENI=$HUBO_248_4_ENI
          /usr/local/bin/aws ec2 attach-network-interface --network-interface-id $RENI --instance-id $INSTANCE_ID --device-index 1 --region ap-northeast-2
          sleep 3
          IFCONFIG_248
          sync_to_fsapp
          #set hostname
          HNAME="ppmdapo9"
          #echo "$R $HNAME" >> /etc/hosts
          /usr/bin/hostnamectl set-hostname $HNAME
          #Change ec2 tag Name
          TAGNAME="$CZNAME-$HNAME"
          /usr/local/bin/aws ec2 create-tags --resources $INSTANCE_ID --tags 'Key="Name",Value="'"$TAGNAME"'"' --region ap-northeast-2
          sleep 9
          /bin/sed -i 's/PRD\-LiivMate\-ppmdapo2\-Was/PRD\-LiivMate\-ppmdapo9\-Was/g' /var/awslogs/etc/awslogs.conf
          sleep 2
          #ITSM_REGI
          execute_service
          exit 0
        fi
   fi
 done
elif [[ $CHK_PRESUB == "1095250" ]]
then
 HUBO_250_0="10.95.250.200"
 HUBO_250_0_ENI="eni-03f9a4bffbcb69b53"
 HUBO_250_1="10.95.250.201"
 HUBO_250_1_ENI="eni-09186ee3393204fab"
 HUBO_250_2="10.95.250.202"
 HUBO_250_2_ENI="eni-0069c56b6ccef78de"
 HUBO_250_3="10.95.250.203"
 HUBO_250_3_ENI="eni-0c89a95bc1275ea31"
 HUBO_250_4="10.95.250.204"
 HUBO_250_4_ENI="eni-00cd0bb00203285f7"
#
 arr=($HUBO_250_0 $HUBO_250_1 $HUBO_250_2 $HUBO_250_3 $HUBO_250_4)
#
# #echo ${#arr[@]}
 for ip in $(seq 0 ${#arr[@]})
 do
  INUSE=$(/usr/local/bin/aws ec2 describe-network-interfaces --filters Name=addresses.private-ip-address,Values="${arr[ip]}" --region ap-northeast-2 | grep -i status | tail -1 | awk -F':' '{print $2}' | sed 's/\,//g' | sed 's/^ //g')
  echo $INUSE
  #echo ${arr[$ip]}
   #ping -c 3 ${arr[$ip]}
   if [[ $INUSE != "\"in-use\"" ]]
   then
        R=${arr[$ip]}
        echo $R
        if [[ $R == "$HUBO_250_0" ]]
        then
          RENI=$HUBO_250_0_ENI
          /usr/local/bin/aws ec2 attach-network-interface --network-interface-id $RENI --instance-id $INSTANCE_ID --device-index 1 --region ap-northeast-2
          sleep 3
          IFCONFIG_250
          sync_to_fsapp
          #set hostname
          HNAME="ppmdapo2"
          #echo "$R $HNAME" >> /etc/hosts
          /usr/bin/hostnamectl set-hostname $HNAME
          #Change ec2 tag Name
          TAGNAME="$AZNAME-$HNAME"
          /usr/local/bin/aws ec2 create-tags --resources $INSTANCE_ID --tags 'Key="Name",Value="'"$TAGNAME"'"' --region ap-northeast-2
          sleep 9
          /bin/sed -i 's/PRD\-LiivMate\-ppmdapo2\-Was/PRD\-LiivMate\-ppmdapo2\-Was/g' /var/awslogs/etc/awslogs.conf
          sleep 2
          #ITSM_REGI
          execute_service
          #run this instance in only WAS 1,2
          sudo su - wasadm -c "cd /fswas/domains/uaadomain; ./startkatad301_i01.sh" > /dev/null 2>&1
          sleep 5
          exit 0
        elif [[ $R == "$HUBO_250_1" ]]
        then
          RENI=$HUBO_250_1_ENI
          /usr/local/bin/aws ec2 attach-network-interface --network-interface-id $RENI --instance-id $INSTANCE_ID --device-index 1 --region ap-northeast-2
          sleep 3
          IFCONFIG_250
          sync_to_fsapp
          #set hostname
          HNAME="ppmdapo4"
          #echo "$R $HNAME" >> /etc/hosts
          /usr/bin/hostnamectl set-hostname $HNAME
          #Change ec2 tag Name
          TAGNAME="$AZNAME-$HNAME"
          /usr/local/bin/aws ec2 create-tags --resources $INSTANCE_ID --tags 'Key="Name",Value="'"$TAGNAME"'"' --region ap-northeast-2
          sleep 9
          /bin/sed -i 's/PRD\-LiivMate\-ppmdapo2\-Was/PRD\-LiivMate\-ppmdapo4\-Was/g' /var/awslogs/etc/awslogs.conf
          sleep 2
          #ITSM_REGI
          execute_service
          exit 0
        elif [[ $R == "$HUBO_250_2" ]]
        then
          RENI=$HUBO_250_2_ENI
          /usr/local/bin/aws ec2 attach-network-interface --network-interface-id $RENI --instance-id $INSTANCE_ID --device-index 1 --region ap-northeast-2
          sleep 3
          IFCONFIG_250
          sync_to_fsapp
          #set hostname
          HNAME="ppmdapo6"
          #echo "$R $HNAME" >> /etc/hosts
          /usr/bin/hostnamectl set-hostname $HNAME
          #Change ec2 tag Name
          TAGNAME="$AZNAME-$HNAME"
          /usr/local/bin/aws ec2 create-tags --resources $INSTANCE_ID --tags 'Key="Name",Value="'"$TAGNAME"'"' --region ap-northeast-2
          sleep 9
          /bin/sed -i 's/PRD\-LiivMate\-ppmdapo2\-Was/PRD\-LiivMate\-ppmdapo6\-Was/g' /var/awslogs/etc/awslogs.conf
          sleep 2
          #ITSM_REGI
          execute_service
          exit 0
        elif [[ $R == "$HUBO_250_3" ]]
        then
          RENI=$HUBO_250_3_ENI
          /usr/local/bin/aws ec2 attach-network-interface --network-interface-id $RENI --instance-id $INSTANCE_ID --device-index 1 --region ap-northeast-2
          sleep 3
          IFCONFIG_250
          sync_to_fsapp
          #set hostname
          HNAME="ppmdapo8"
          #echo "$R $HNAME" >> /etc/hosts
          /usr/bin/hostnamectl set-hostname $HNAME
          #Change ec2 tag Name
          TAGNAME="$AZNAME-$HNAME"
          /usr/local/bin/aws ec2 create-tags --resources $INSTANCE_ID --tags 'Key="Name",Value="'"$TAGNAME"'"' --region ap-northeast-2
          sleep 9
          /bin/sed -i 's/PRD\-LiivMate\-ppmdapo2\-Was/PRD\-LiivMate\-ppmdapo8\-Was/g' /var/awslogs/etc/awslogs.conf
          sleep 2
          #ITSM_REGI
          execute_service
          exit 0
        elif [[ $R == "$HUBO_250_4" ]]
        then
          RENI=$HUBO_250_4_ENI
          /usr/local/bin/aws ec2 attach-network-interface --network-interface-id $RENI --instance-id $INSTANCE_ID --device-index 1 --region ap-northeast-2
          sleep 3
          IFCONFIG_250
          sync_to_fsapp
          #set hostname
          HNAME="ppmdapoa"
          #echo "$R $HNAME" >> /etc/hosts
          /usr/bin/hostnamectl set-hostname $HNAME
          #Change ec2 tag Name
          TAGNAME="$AZNAME-$HNAME"
          /usr/local/bin/aws ec2 create-tags --resources $INSTANCE_ID --tags 'Key="Name",Value="'"$TAGNAME"'"' --region ap-northeast-2
          sleep 9
          /bin/sed -i 's/PRD\-LiivMate\-ppmdapo2\-Was/PRD\-LiivMate\-ppmdapoa\-Was/g' /var/awslogs/etc/awslogs.conf
          sleep 2
          #ITSM_REGI
          execute_service
          exit 0
        fi
   fi
 done
fi
```