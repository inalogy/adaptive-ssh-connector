#!/bin/csh

# example: csh /home/svc-midpoint/deleteScript.sh -uid "5001"
#FIXME: ONLY FOR TESTING PURPOSES DO NOT USE IN PRODUCTION
# Get UID from the command-line arguments
set uid = "$argv[2]"

# Find the username associated with the given UID
set username = `awk -F: -v uid="$uid" '$3 == uid { print $1 }' /etc/passwd`

if ("$username" == "") then
  echo "No user found with UID $uid."
  exit 1
endif

# Delete the user
doas userdel $username

#remove user home directory
doas rm -r /home/$username

