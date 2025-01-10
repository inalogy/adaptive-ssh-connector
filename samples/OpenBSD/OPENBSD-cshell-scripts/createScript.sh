#!/bin/csh

#example: csh /home/svc-midpoint/createScript.sh -login "hruska099" -fullName "Blazej~Hruska" -password "SuperSecretPassword"

# Initialize variables that will store our args
set type = ""
set login = ""
set fullName = ""
set password = ""
set STARTUID = 4000

# Parse the named arguments
@ i = 1
while ($i <= $#argv)
  switch ($argv[$i])
    case "-login":
      @ i++
      set login = $argv[$i]
      breaksw
    case "-fullName":
      @ i++
      set fullName = $argv[$i]
      set fullName = ${fullName:gs/~/ /} # replace tilde with white space based on connector configuration
      breaksw
    case "-password":
      @ i++
      set password = $argv[$i]
      breaksw
  endsw
  @ i++
end

# Validate that all required arguments have been provided
if ("$type" == "" || "$login" == "" || "$fullName" == "" || "$password" == "") then
  echo "FATAL_ERROR: Missing required parameters."
  exit 1
endif


  #main command
  doas adduser -batch $login "$fullName" $password -uid_start $STARTUID -group $GIDNAME -message no -shell "someshell" -home $UNIX_HOME_DIR -unencrypted -dotdir no > /dev/null

# in our case icfsName == login
set user_uid = `id -u $login`
  if ($user_uid != "")then
    echo "uid"
    echo "$user_uid"
  else echo "FATAL_ERROR Couldn't create user: $login"
  endif
