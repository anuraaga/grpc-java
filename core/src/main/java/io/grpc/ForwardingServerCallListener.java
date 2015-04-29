/*
 * Copyright 2015, Google Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *    * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *
 *    * Neither the name of Google Inc. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.grpc;

/**
 * A {@link ServerCall.Listener} which forwards all of its methods to another {@link
 * ServerCall.Listener}.
 */
public abstract class ForwardingServerCallListener<ReqT> extends ServerCall.Listener<ReqT> {
  /**
   * Returns the delegated {@code ServerCall.Listener}.
   */
  protected abstract ServerCall.Listener<ReqT> delegate();

  @Override
  public void onPayload(ReqT payload) {
    delegate().onPayload(payload);
  }

  @Override
  public void onHalfClose() {
    delegate().onHalfClose();
  }

  @Override
  public void onCancel() {
    delegate().onCancel();
  }

  @Override
  public void onComplete() {
    delegate().onComplete();
  }

  @Override
  public void onReady(int numMessages) {
    delegate().onReady(numMessages);
  }

  /**
   * A simplified version of {@link ForwardingServerCallListener} where subclasses can pass in a
   * {@link ServerCall.Listener} as the delegate.
   */
  public abstract static class SimpleForwardingServerCallListener<ReqT>
      extends ForwardingServerCallListener<ReqT> {

    private final ServerCall.Listener<ReqT> delegate;

    protected SimpleForwardingServerCallListener(ServerCall.Listener<ReqT> delegate) {
      this.delegate = delegate;
    }

    @Override
    protected ServerCall.Listener<ReqT> delegate() {
      return delegate;
    }
  }
}