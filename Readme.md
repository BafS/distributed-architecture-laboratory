# Distributed architecture laboratory

## Protocol

 - Timeout: 1 second
 - Communication: Using binary format

## Architecture

 - n Services
 - n Linker (public IPs, cannot add new linkers after initialization)
 - n Clients

If a linker, service or client does not reply after the timeout, it is considered as dead.



Client --ask service info--> Linker (#rand)
       <--service info------/

Service --register himself--> Linker

