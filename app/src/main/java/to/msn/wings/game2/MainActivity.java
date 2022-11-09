package to.msn.wings.game2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    boolean[][] set = new boolean[5][5];
    Button[][] btn =new Button[5][5];
    int i,j;
    int turn = 0; //クリアまでのターン数
    int count = 0; //問題数
    int nowPlay = 0; //現在選択中の問題
    boolean editmode = false; //問題編集モード
    boolean reset = true; //ゲームリセット判定
    String createData = "0000000000000000000000000"; //編集データ

    SimpleDatabaseHelper helper = null;

    ArrayList<String> qname = new ArrayList<>();
    ArrayList<boolean[][]> q = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //経過ターン、クリア時表示
        TextView message = findViewById(R.id.message);
        message.setText("待機中");

        //ヘルパー準備
        helper = new SimpleDatabaseHelper(this);

        String[] datas = {"data"};
        try(SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cs = db.query("pdata",datas,null,null,null,null,null,null)){
            q.clear();
            qname.clear();
            boolean next = cs.moveToFirst();

            if(next){
                while(next){
                    String bdata = cs.getString(0);
                    String[] bdatas = bdata.split("");
                    boolean[][] kdata = new boolean[5][5];
                    for(int i=0;i<5;i++){
                        for(int j=0;j<5;j++){
                            if(bdatas[i*5+j].equals("1")) {
                                kdata[i][j] = true;
                            }else if(bdatas[i*5+j].equals("0")){
                                kdata[i][j] = false;
                            }
                            Log.e("Q"+count+":"+i+j,""+kdata[i][j]);
                        }
                    }
                    q.add(kdata);
                    qname.add("問題"+(count+1));
                    count++;
                    next = cs.moveToNext();
                }

            }else{
                Toast.makeText(this,"データが読み込めませんでした",Toast.LENGTH_SHORT).show();
            }
        }

        for(i=0; i<5 ; i++) {
            for(j=0;j<5;j++){

                btn[i][j] = new Button(this);
                set[i][j] = false; //全ボタンとも初期状態はOFF

                //各ボタンを取得
                int btnId = getResources().getIdentifier("btn"+i+j, "id", getPackageName());
                btn[i][j] = findViewById(btnId);
                btn[i][j].setEnabled(false); //初期はボタン無効

                // lambda型
                btn[i][j].setOnClickListener( v -> {

                    a:
                    for (int k = 0; k < 5; k++) {
                        for (int l = 0; l < 5; l++) {
                            if (v == btn[k][l]) {
                                i = k;
                                j = l;
                                break a;
                            }
                        }
                    }

                    if(editmode == false) {
                        turn++;
                        message.setText(turn + "ターン目です");


                        //クリックしたボタンのオンオフ切り替え
                        if (set[i][j] == false) {
                            btnOn(i, j);

                        } else {
                            btnOff(i, j);
                        }

                        //上ボタンのオンオフ切り替え
                        if (i - 1 >= 0 && set[i - 1][j] == false) {
                            btnOn(i - 1, j);
                        } else if (i - 1 >= 0 && set[i - 1][j] == true) {
                            btnOff(i - 1, j);
                        }

                        //左ボタンのオンオフ切り替え
                        if (j - 1 >= 0 && set[i][j - 1] == false) {
                            btnOn(i, j - 1);
                        } else if (j - 1 >= 0 && set[i][j - 1] == true) {
                            btnOff(i, j - 1);
                        }

                        //右ボタンのオンオフ切り替え
                        if (j + 1 <= 4 && set[i][j + 1] == false) {
                            btnOn(i, j + 1);
                        } else if (j + 1 <= 4 && set[i][j + 1] == true) {
                            btnOff(i, j + 1);
                        }

                        //下ボタンのオンオフ切り替え
                        if (i + 1 <= 4 && set[i + 1][j] == false) {
                            btnOn(i + 1, j);
                        } else if (i + 1 <= 4 && set[i + 1][j] == true) {
                            btnOff(i + 1, j);
                        }

                        boolean hante = true; //全部OFFになったか判定

                        all:
                        for (int k = 0; k < 5; k++) {
                            for (int l = 0; l < 5; l++) {
                                if (set[k][l] == true) { //ON状態のボタンが見つかったら
                                    hante = false;
                                    break all;
                                }
                            }
                        }

                        if (hante == true) {
                            String msg = "ゲームクリア！";
                            message.setText(msg);

                            try(SQLiteDatabase db = helper.getWritableDatabase()){ //記録の登録

                                ContentValues cv = new ContentValues();
                                cv.put("turn", turn);
                                cv.put("qnum", nowPlay);
                                db.insert("records", null, cv);

                            }

                                String[] cols = {"turn"};
                                String[] params = { ""+nowPlay };
                                String orderby = "turn ASC";
                                try(SQLiteDatabase db = helper.getReadableDatabase()){
                                    Cursor cs = db.query("records",cols,"qnum=?",params,null,null,orderby,"10");
                                    boolean next = cs.moveToFirst();
                                    ArrayList<String> turns = new ArrayList<>();
                                    int no = 0;
                                    boolean dup = false;
                                    while(next){
                                        if(turn == cs.getInt(0) && dup == false){
                                            turns.add("No."+(no+1) + "　ターン数:"+ cs.getInt(0)+" NEW!");
                                            dup = true;
                                        }else{
                                            turns.add("No."+(no+1) + "　ターン数:"+ cs.getInt(0));
                                        }
                                        no++;
                                        if(no>=10)
                                            break;
                                        next = cs.moveToNext();
                                    }
                                    DialogFragment dialog = new RankDialogFragment();
                                    Bundle data = new Bundle();
                                    data.putString("a","AAA");
                                    data.putStringArrayList("turns",turns);
                                    dialog.setArguments(data);
                                    dialog.show(getSupportFragmentManager(),"dialog_basic");

                            }


                            for (int i = 0; i < 5; i++) { //全ボタン無効化
                                for (int j = 0; j < 5; j++) {
                                    btn[i][j].setEnabled(false);
                                }
                            }
                        }
                    }else{ //編集モード時


                        //クリックしたボタンのオンオフ切り替え
                        if(set[i][j]==false){
                            btnOn(i,j);
                            createData = createData.substring(0,i*5+j) + "1" + createData.substring(i*5+j+1,25);
                        }else{
                            btnOff(i,j);
                            createData = createData.substring(0,i*5+j) + "0" + createData.substring(i*5+j+1,25);
                        }

                        //上ボタンのオンオフ切り替え
                        if (i - 1 >= 0 && set[i - 1][j] == false) {
                            btnOn(i - 1, j);
                            createData = createData.substring(0,(i-1)*5+j) + "1" + createData.substring((i-1)*5+j+1,25);
                        } else if (i - 1 >= 0 && set[i - 1][j] == true) {
                            btnOff(i - 1, j);
                            createData = createData.substring(0,(i-1)*5+j) + "0" + createData.substring((i-1)*5+j+1,25);
                        }

                        //下ボタンのオンオフ切り替え
                        if (i + 1 <= 4 && set[i + 1][j] == false) {
                            btnOn(i + 1, j);
                            createData = createData.substring(0,(i+1)*5+j) + "1" + createData.substring((i+1)*5+j+1,25);
                        } else if (i + 1 <= 4 && set[i + 1][j] == true) {
                            btnOff(i + 1, j);
                            createData = createData.substring(0,(i+1)*5+j) + "0" + createData.substring((i+1)*5+j+1,25);
                        }


                        //左ボタンのオンオフ切り替え
                        if (j - 1 >= 0 && set[i][j - 1] == false) {
                            btnOn(i, j - 1);
                            createData = createData.substring(0,i*5+j-1) + "1" + createData.substring(i*5+j,25);
                        } else if (j - 1 >= 0 && set[i][j - 1] == true) {
                            btnOff(i, j - 1);
                            createData = createData.substring(0,i*5+j-1) + "0" + createData.substring(i*5+j,25);
                        }

                        //右ボタンのオンオフ切り替え
                        if (j + 1 <= 4 && set[i][j + 1] == false) {
                            btnOn(i, j + 1);
                            createData = createData.substring(0,i*5+j+1) + "1" + createData.substring(i*5+j+2,25);
                        } else if (j + 1 <= 4 && set[i][j + 1] == true) {
                            btnOff(i, j + 1);
                            createData = createData.substring(0,i*5+j+1) + "0" + createData.substring(i*5+j+2,25);
                        }



                    }

                });


            }


        }

        //Spinner取得して問題を入れる
        Spinner stage = findViewById(R.id.stage);

        ArrayAdapter<String> Adapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,qname);
        stage.setAdapter(Adapter);

        stage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                nowPlay = i;
                for(int j=0;j<5;j++){
                    for(int k=0;k<5;k++){
                        if(q.get(i)[j][k]==false){
                            btnOff(j,k);
                        }else{
                            btnOn(j,k);
                        }
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        Button register = findViewById(R.id.register);
        register.setOnClickListener(v->{
            if(editmode == false){
                for(int i=0;i<5;i++){
                    for(int j=0;j<5;j++){
                        btnOff(i,j);
                        btn[i][j].setEnabled(true);
                    }
                }
                message.setText("編集中");
                register.setText("編集完了");
                editmode = true; //編集モード
            }else{

                try(SQLiteDatabase db = helper.getWritableDatabase()){
                    ContentValues cv = new ContentValues();
                    cv.put("data",createData);
                    db.insert("pdata",null,cv);



                    String[] bdatas = createData.split("");
                    boolean[][] kdata = new boolean[5][5];
                    for(int i=0;i<5;i++){
                        for(int j=0;j<5;j++){
                            if(bdatas[i*5+j].equals("1")) {
                                kdata[i][j] = true;
                            }else if(bdatas[i*5+j].equals("0")){
                                kdata[i][j] = false;
                            }
                            Log.e("Q"+count+":"+i+j,""+kdata[i][j]);
                        }
                    }
                    q.add(kdata);
                    qname.add("問題"+(count+1));
                    count++;


                    for(int i=0;i<5;i++){
                        for(int j=0;j<5;j++){
                            btn[i][j].setEnabled(false);

                        }
                    }

                    createData = "0000000000000000000000000"; //データ初期化
                    Toast.makeText(this,"データ登録完了",Toast.LENGTH_SHORT).show();

                    message.setText("待機中");
                    register.setText("問題登録");
                    editmode = false;
                }
            }

        });

        Button start = findViewById(R.id.startBtn);
        start.setOnClickListener(v-> { //スタートしたらボタンをすべて有効化
            if(reset == true) {
                for (int i = 0; i < 5; i++) {
                    for (int j = 0; j < 5; j++) {
                        btn[i][j].setEnabled(true);
                    }
                }
                turn = 0;
                message.setText("プレイスタート");
                start.setText("RESET");
                reset = false;
            }else{

                for(int j=0;j<5;j++){
                    for(int k=0;k<5;k++){
                        btn[j][k].setEnabled(false);
                        if(q.get(nowPlay)[j][k]==false){
                            btnOff(j,k);
                        }else{
                            btnOn(j,k);
                        }
                    }
                }

                message.setText("待機中");
                start.setText("START");
                reset =true;
            }
        });


    }
    void btnOn(int i1,int j1){
        btn[i1][j1].setBackgroundColor(Color.rgb(94, 53, 177));
        set[i1][j1] = true;
    }

    void btnOff(int i2,int j2){
        btn[i2][j2].setBackgroundColor(Color.rgb(245, 241, 255));
        set[i2][j2] = false;
    }

}