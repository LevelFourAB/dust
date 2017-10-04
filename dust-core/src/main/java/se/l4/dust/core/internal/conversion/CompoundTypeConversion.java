/*
 * Copyright 2008 Andreas Holstenson
 *
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.l4.dust.core.internal.conversion;

import se.l4.dust.api.conversion.NonGenericConversion;

/**
 * Compound conversion used for chaining two type conversions together.
 *
 * @author Andreas Holstenson
 *
 */
public class CompoundTypeConversion
	implements NonGenericConversion<Object, Object>
{
	private final NonGenericConversion<Object, Object> in;
	private final NonGenericConversion<Object, Object> out;

	public CompoundTypeConversion(
		NonGenericConversion<Object, Object> in,
		NonGenericConversion<Object, Object> out)
	{
		this.in = in;
		this.out = out;
	}

	public NonGenericConversion<Object, Object> getIn()
	{
		return in;
	}

	public NonGenericConversion<Object, Object> getOut()
	{
		return out;
	}

	public Object convert(Object in)
	{
		Object firstPass = this.in.convert(in);
		return out.convert(firstPass);
	}

	public Class<Object> getInput()
	{
		return in.getInput();
	}

	public Class<Object> getOutput()
	{
		return out.getOutput();
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + in + " => " + out + "]";
	}
}
