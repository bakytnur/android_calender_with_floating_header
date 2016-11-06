# outlook_test_pjt

# data folder is for mostly POJO objects. So variable naming in those folders don't start with 'm'. I followed java coding styles.
# In android folder, all fields starts with 'm' to differentiate with local variables. 
# The database will be empty initially. All event data are saved on the database, and synchronized, except Account information which I hardcoded for a sample accounts.
# I tried to follow Outlook calendar GUI/concept as much as possible. There might be few minor GUI issues which I can handle if I have little more time.

# All events are divided to timeSlots. A timeSlot is an object which keeps a data of a single day. So, events which covers 3 days are divided into 3 timeSlots.
# I used icons from Google https://material.io/icons/#ic_alarm 

