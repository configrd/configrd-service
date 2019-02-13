#!/bin/bash

/opt/hashicorp/vault server -dev &
export VAULT_ADDR=http://127.0.0.1:8200
sleep 2
/opt/hashicorp/vault auth enable userpass
/opt/hashicorp/vault write auth/userpass/users/test password=password policies=admins
/opt/hashicorp/vault policy write admins src/test/resources/vault/admin.hcl
