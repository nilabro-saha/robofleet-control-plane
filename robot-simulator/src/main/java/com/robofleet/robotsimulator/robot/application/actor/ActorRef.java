package com.robofleet.robotsimulator.robot.application.actor;

/**
 * Minimal actor-reference contract for asynchronous-style message delivery.
 *
 * <p>Intent: provide a stable abstraction for sending messages to actor-backed runtime objects
 * without exposing internal mutable state to callers.</p>
 *
 * @param <MessageT> command/message type accepted by actor reference
 * @author Nilabro Saha
 */
public interface ActorRef<MessageT> {

  /**
   * Delivers one message to the actor reference.
   *
   * @param message message to process
   */
  void tell(MessageT message);
}
