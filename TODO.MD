TODO:

1- add time stamp to sqrlcache entry.
We will need this to cleanup expired cache entries. I haven found any process for cleaning up abandoned cache entries in the code.


2- Redirect to home after sucessfull linking.

3- Cleanup restart process
   - Remove unnecessary wildfly container
   - Remove -v on init scripts.