/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.core;

import java.io.File;
import java.io.IOException;

public interface FrameworkLocationProvider
{
   File aquireFrameworkLocation() throws IOException;

   void releaseFrameworkLocation(File frameworkLocation) throws IOException;
}
