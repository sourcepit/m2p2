/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.osgi.embedder;

import java.net.URL;

import org.sourcepit.common.utils.collections.Iterable2;

public interface BundleProvider<E extends Exception>
{
   Iterable2<URL, E> getFrameworkJARs() throws E;

   Iterable2<URL, E> getBundleJARs() throws E;

}
