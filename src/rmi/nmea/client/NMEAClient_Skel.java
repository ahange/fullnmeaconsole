// Skeleton class generated by rmic, do not edit.
// Contents subject to change without notice.

package rmi.nmea.client;

public final class NMEAClient_Skel
    implements java.rmi.server.Skeleton
{
    private static final java.rmi.server.Operation[] operations = {
	new java.rmi.server.Operation("java.lang.String getAddress()"),
	new java.rmi.server.Operation("void notify(nmea.server.ctx.NMEADataCache)")
    };
    
    private static final long interfaceHash = -7285216383323056028L;
    
    public java.rmi.server.Operation[] getOperations() {
	return (java.rmi.server.Operation[]) operations.clone();
    }
    
    public void dispatch(java.rmi.Remote obj, java.rmi.server.RemoteCall call, int opnum, long hash)
	throws java.lang.Exception
    {
	if (opnum < 0) {
	    if (hash == 7650440960692401886L) {
		opnum = 0;
	    } else if (hash == -4935293009707984834L) {
		opnum = 1;
	    } else {
		throw new java.rmi.UnmarshalException("invalid method hash");
	    }
	} else {
	    if (hash != interfaceHash)
		throw new java.rmi.server.SkeletonMismatchException("interface hash mismatch");
	}
	
	rmi.nmea.client.NMEAClient server = (rmi.nmea.client.NMEAClient) obj;
	switch (opnum) {
	case 0: // getAddress()
	{
	    call.releaseInputStream();
	    java.lang.String $result = server.getAddress();
	    try {
		java.io.ObjectOutput out = call.getResultStream(true);
		out.writeObject($result);
	    } catch (java.io.IOException e) {
		throw new java.rmi.MarshalException("error marshalling return", e);
	    }
	    break;
	}
	    
	case 1: // notify(NMEADataCache)
	{
	    nmea.server.ctx.NMEADataCache $param_NMEADataCache_1;
	    try {
		java.io.ObjectInput in = call.getInputStream();
		$param_NMEADataCache_1 = (nmea.server.ctx.NMEADataCache) in.readObject();
	    } catch (java.io.IOException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } catch (java.lang.ClassNotFoundException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } finally {
		call.releaseInputStream();
	    }
	    server.notify($param_NMEADataCache_1);
	    try {
		call.getResultStream(true);
	    } catch (java.io.IOException e) {
		throw new java.rmi.MarshalException("error marshalling return", e);
	    }
	    break;
	}
	    
	default:
	    throw new java.rmi.UnmarshalException("invalid method number");
	}
    }
}
