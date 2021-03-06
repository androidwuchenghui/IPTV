/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.iptv.hn;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.iptv.hn.service.BootloaderService;

/*
 * MainActivity class that loads MainFragment
 */
public class MainActivity extends Activity implements View.OnClickListener {

    private View mClickView;
//   测试1
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.layout_test);

        mClickView = findViewById(R.id.test);
        mClickView.setOnClickListener(this);

        Intent intentService = new Intent(this, BootloaderService.class);
        startService(intentService);

        finish();

    }

    @Override
    public void onClick(View v) {

    }

}
