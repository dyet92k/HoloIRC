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

package com.fusionx.lightirc.promptdialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;
import com.fusionx.lightirc.R;

public abstract class PromptDialog extends AlertDialog.Builder implements DialogInterface.OnClickListener {
    private final EditText input;

    public PromptDialog(final Context context, final String title, final String hint, final String edittextdefaulttext) {
        super(context);

        setTitle(title);

        input = new EditText(context);
        input.setHint(hint);
        input.append(edittextdefaulttext);
        input.setSingleLine(true);
        setView(input);

        setPositiveButton(context.getString(R.string.ok), this);

        setNegativeButton(context.getString(R.string.cancel), this);
    }

    @Override
    public void onClick(final DialogInterface dialog, final int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            onOkClicked(input.getText().toString());
        }
        dialog.dismiss();
    }

    abstract public void onOkClicked(final String input);
}
