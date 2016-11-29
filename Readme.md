# Distributed architecture laboratory

## Protocol

 - Timeout: 1 second
 - Communication: Using binary format

## Architecture

 - n Services
 - m Linker (public IPs, cannot add new linkers after initialization)
 - k Clients

If a linker, service or client does not reply after the timeout, it is considered as dead.

# Launching linkers:

`java Linker <linker id>`

The linker id is the line number in linker.txt, associating a specific id to a port

Example: `java linker 1`

# Launching Services:

`java Service <type> <port>`

type: Type of service
    - "reply"
    - "time"
    - "sum"
    
port: Port of service

# Launching clients:

`java Client <type> <port>`

Same specifics as with services.