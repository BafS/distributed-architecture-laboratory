/**
 * The client knows the linkers and want to use a service
 *
 * To use a service, the client firstly asks randomly one of the linker to give him the host and
 * port of the service.
 * If the service does not reply, the client will ask again, randomly, a linker.
 *
 * 1. Ask a random linker the address of a specific service
 * 2a. if OK
 * 2b. if Error -> goto 1.
 */
public class Client {
}
