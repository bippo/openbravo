#!/bin/sh

# *************************************************************************
# * The contents of this file are subject to the Openbravo  Public  License
# * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
# * Version 1.1  with a permitted attribution clause; you may not  use this
# * file except in compliance with the License. You  may  obtain  a copy of
# * the License at http://www.openbravo.com/legal/license.html 
# * Software distributed under the License  is  distributed  on  an "AS IS"
# * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
# * License for the specific  language  governing  rights  and  limitations
# * under the License. 
# * The Original Code is Openbravo ERP. 
# * The Initial Developer of the Original Code is Openbravo SLU 
# * All portions are Copyright (C) 2008 Openbravo SLU 
# * All Rights Reserved. 
# * Contributor(s):  ______________________________________.
# ************************************************************************


# Check a user's permissions in a directory

if [ $# -lt 1 ]; then
  echo "Usage: check-ob-perms.sh directory [username]"
  exit 1
elif [ ! -d "$1" ]; then
  echo "Error: specified directory does not exist: $2"
  exit 1
fi

if [ $# -lt 2 ]; then
  USER=${whoami}
else
  USER=$2
fi

USERID=$(id -u $USER)
USERGROUPS="$(id -G $USER)"
DIRFILES=$(find "$1" -printf "%U-%G+%m_%p\n")

# If find fails, there is no read permission or the file does not exist
if [ $? -eq 1 ]; then
  echo "Error: user $USER does not have read permissions in some files or directories"
  exit 1
fi

IFS='
'

for FILEDATA in $DIRFILES; do
  # get part until first -
  OWNER=${FILEDATA%%-*}
  # get part until first +, but after -
  mytemp=${FILEDATA%%+*}
  GROUP=${mytemp#*-}
  # get part until first -, but after +
  mytemp=${FILEDATA%%_*}
  PERM=${mytemp#*+}
  # get part after first _
  FILE=${FILEDATA#*_}

  if [ $USERID -ne $OWNER ]; then
    
    # Check if user belongs to the file's group
    echo $USERGROUPS | grep -q $GROUP
    [ $? -eq 0 ] && BELONGS=1 || BELONGS=0

    # Significant bit
    if [ $BELONGS -eq 1 ]; then
      SBIT=$(echo $PERM | sed 's/.\(.\)./\1/')
    else
      SBIT=$(echo $PERM | sed 's/..\(.\)/\1/')
    fi

    if [ $SBIT -ne 6 ] && [ $SBIT -ne 7 ]; then
      echo "Error: user $USER does not have read or write permissions in file or directory:"
      echo "$FILE"
      exit 1
    fi

  fi

done
