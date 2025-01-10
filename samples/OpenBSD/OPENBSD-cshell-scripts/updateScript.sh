#!/bin/csh

# example: csh /home/svc-midpoint/updateScript.sh -uid "5001" -groups "ADD:group2","ADD:group3","REMOVE:unixgroup" -fullName "Blazej~Hruskai"

set uid = ""
set fullName = ""
set groupsToAdd = ""
set groupsToRemove = ""

# Parsing arguments
set i = 1
while ($i <= $#argv)
  switch ($argv[$i])
    case "-uid":
      @ i++
      set uid = $argv[$i]
      breaksw
    case "-groups":
      @ i++
      foreach group_action (`echo $argv[$i] | tr ',' ' '`)
        switch ($group_action:q)
          case "ADD:*":
            set groupsToAdd = "$groupsToAdd `echo $group_action | sed 's/ADD://g'`"
            breaksw
          case "REMOVE:*":
            set groupsToRemove = "$groupsToRemove `echo $group_action | sed 's/REMOVE://g'`"
            breaksw
        endsw
      end
      breaksw
    case "-fullName":
      @ i++
      set fullName = "$argv[$i]"
      set fullName = ${fullName:gs/~/ /} # replace tilde with white space
      breaksw
  endsw
  @ i++
end

# Finding username by UID
set username = `awk -F: -v uid="$uid" '$3 == uid { print $1 }' /etc/passwd`

## Updating the groups
if ("$groupsToRemove" != "") then
  set groupsToRemoveList = `echo $groupsToRemove | tr ' ' '\n'`
  foreach group ($groupsToRemoveList)
    doas usermod -G "" -g $group $username
  end
endif

if ("$groupsToAdd" != "") then
  set groupsToAddList = `echo $groupsToAdd | tr ' ' '\n'`
  foreach group ($groupsToAddList)
    doas usermod -G $group $username
  end
endif

if ("$fullName" != "") then
  doas usermod -c "$fullName" $username
endif

