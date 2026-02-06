package nova.mjs.domain.thingo.broadcast.exception;

import nova.mjs.util.exception.ErrorCode;

public class BroadcastSyncException extends BroadcastException {
    public BroadcastSyncException() {
        super(ErrorCode.BROADCAST_SYNC_FAILED);

    }
}
