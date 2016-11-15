# Distributed architecture laboratory

## Protocol

 - Timeout: 1 second
 - Communication: Using the JSON format

## Architecture

 - n Services
 - n Linkers (public IPs)
 - n Clients

If a linker, service or client does not reply after the timeout, it is considered as dead.