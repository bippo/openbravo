
>> Enabling precommit checking:

Add these lines to your hgrc file in the .hg directory in your module:

[hooks]
precommit = ../org.openbravo.client.kernel/jslint/jscheck-hg

>> Running jslint directly for a module
To run jslint directly for a module, go to the module directory and do:

 ../org.openbravo.client.kernel/jslint/jscheck 

NOTE:
- it is possible that you have to set the executable flag on the jslint and jscheck scripts in org.openbravo.client.kernel/jslint.
