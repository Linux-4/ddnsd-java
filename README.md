# ddnsd-java  
Author: Tim Zimmermann <tim.zimmermann@linux4.de>  
  
## Description:   
DDDNSD-Java is a background service to dynamically update  
your IP-Adress in a DNS Zone file. It is compatible  
with most linux / unix based operating systems     

## NOTICE  
You will probably need a secondary nameserver with  
an static IP-Address to which your nameserver will  
send the updated zone, or another dynamic domain  
which is always up to date. For example using [puck.nether.net](https://puck.nether.net/dns)   
or [he.net](https://dns.he.net) which are both supported by this program   
(dynamic master ip update).  

## Requirements:    
- Linux / Unix based operating system  
- Java (8+) 
- Systemd  
- dns server software like bind9 installed on localhost  
  and allowing requests from localhost  
  
## Build Requirements  
- Linux / Unix based operating system  
- maven  

## Installation:  
- Manually install the generated jar from target/DDNSD.jar   
OR  
- Follow the instructions at https://deb.linux4.de/add-repo.txt to use the APT Repository  
- execute apt-get install ddnsd-java  
  
## Update  
- Manually update the jar  
OR  
- Use APT (apt-get update && apt-get upgrade)  
    
## Uninstall:  
- Manually uninstall the jar  
OR  
- Use APT (apt-get remove (or purge) ddnsd-java)  
  
## Usage:  
Install the service as shown above and then edit  
/etc/ddns/ddnsd.ini to configure the service.  
