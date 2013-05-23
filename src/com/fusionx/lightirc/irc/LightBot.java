/*
    LightIRC - an IRC client for Android

    Copyright 2013 Lalit Maganti

    This file is part of LightIRC.

    LightIRC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    LightIRC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with LightIRC. If not, see <http://www.gnu.org/licenses/>.
 */

package com.fusionx.lightirc.irc;

import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;

public class LightBot extends PircBotX {
    private String mBuffer = "";

    public LightBot(Configuration configuration) {
        super(configuration);
    }

    public void appendToBuffer(String message) {
        mBuffer += message;
    }

    public String getBuffer() {
        return mBuffer;
    }
}