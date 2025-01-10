#!/bin/csh

# fetch single account/user
# example: csh /home/svc-midpoint/searchScript.sh -uid "5001"

# fetch all
# example2: csh /home/svc-midpoint/searchScript.sh

set uid = ""

set i = 1
while ($i <= $#argv)
  switch ($argv[$i])
    case "-uid":
      @ i++
      set uid = "$argv[$i]"
      breaksw
  endsw
  @ i++
end

set users_found = 0

# Check if a UID was provided as an argument
if ("$uid" != "") then
  set user_info = `awk -F: -v uid="$uid" '$3 == uid { print $1, $3, $5 }' /etc/passwd`
  if ("$user_info" != "") then
    set users_found = 1
    set username = `echo $user_info | awk '{ print $1 }'`
    set fullName = `echo $user_info | cut -d ' ' -f 3-`
    set groups = `groups $username | tr ' ' '~'`
  endif
else
  set userids = `awk -F: '$3 >= 1000 { print $3 }' /etc/passwd`
  if ("$userids" != "") then
    set users_found = 1
  endif
endif

if ($users_found == 1) then
  # Print column header
  echo "uid|login|fullName|groups"

  if ("$uid" != "") then
    if ("$username" == "") set username = "null"
    if ("$fullName" == "") set fullName = "null"
    if ("$groups" == "") set groups = "null"
    echo "${uid}|${username}|${fullName}|${groups}"
  else
    # No specific UID provided; print details for all users
    foreach uid ($userids)
      set user_info = `awk -F: -v uid="$uid" '$3 == uid { print $1, $3, $5 }' /etc/passwd`
      set username = `echo $user_info | awk '{ print $1 }'`
      set fullName = `echo $user_info | cut -d ' ' -f 3-`
      set groups = `groups $username | tr ' ' '~'`
      if ("$username" == "") set username = "null"
      if ("$fullName" == "") set fullName = "null"
      if ("$groups" == "") set groups = "null"
      echo "${uid}|${username}|${fullName}|${groups}"
    end
  endif
endif
