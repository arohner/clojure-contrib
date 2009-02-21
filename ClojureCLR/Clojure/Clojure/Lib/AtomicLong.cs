﻿/**
 *   Copyright (c) David Miller. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;

namespace clojure.lang
{
    /// <summary>
    /// Implements the Java <c>java.util.concurrent.atomic.AtomicLong</c> class.  
    /// </summary>
    /// <remarks>I hope.  Someone with more knowledge of these things should check this out.</remarks>
    public sealed class AtomicLong
    {
        #region Data

        /// <summary>
        /// The current <see cref="Int32">integer</see> value.
        /// </summary>
        long _val;

        #endregion

        #region Ctors

        /// <summary>
        /// Initializes an <see cref="AtomicLong">AtomicLong</see> with value zero.
        /// </summary>
        public AtomicLong()
        {
            _val = 0;
        }

        /// <summary>
        /// Initializes an <see cref="AtomicLong">AtomicLong</see> with a given value.
        /// </summary>
        /// <param name="initVal">The initial value.</param>
        public AtomicLong(long initVal)
        {
            _val = initVal;
        }

        #endregion

        #region Value access

        /// <summary>
        /// Gets the current value.
        /// </summary>
        /// <returns>The current value.</returns>
        public long get() 
        {
            return _val;
        }

        /// <summary>
        /// Increments the value and returns the new value.
        /// </summary>
        /// <returns>The new value.</returns>
        public long incrementAndGet()
        {
            return Interlocked.Increment(ref _val);
        }

        /// <summary>
        /// Increments the value and returns the original value.
        /// </summary>
        /// <returns>The original value.</returns>
        public long getAndIncrement()
        {
            return Interlocked.Increment(ref _val)-1;
        }

        /// <summary>
        /// Sets the value if the expected value is current.
        /// </summary>
        /// <param name="oldVal">The expected value.</param>
        /// <param name="newVal">The new value.</param>
        /// <returns><value>true</value> if the value was set; <value>false</value> otherwise.</returns>
        public bool compareAndSet(long oldVal, long newVal)
        {
            long origVal = Interlocked.CompareExchange(ref _val, newVal, oldVal);
            return origVal == oldVal;
        }

        /// <summary>
        /// Sets the value.
        /// </summary>
        /// <param name="newVal">The new value.</param>
        /// <returns>The new value.</returns>
        public long set(long newVal)
        {
            return Interlocked.Exchange(ref _val,newVal);
        }

        #endregion
    }
}
