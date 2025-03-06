
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expensetracker.R;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class BillAddActivity extends AppCompatActivity implements
        RadioGroup.OnCheckedChangeListener, View.OnClickListener, DatePickerDialog.OnDateSetListener {
    private final static String TAG = "BillAddActivity";
    private TextView tv_date;
    private RadioButton rb_income;
    private RadioButton rb_expand;
    private EditText et_desc;
    private EditText et_amount;
    private int mBillType = 1; // Type of Bill: 0 Income；1 Expense
    private int id; // If id is not null, the bill already exists
    private Calendar calendar = Calendar.getInstance();
    private BillDBHelper mBillHelper; // 声明一个账单数据库的帮助器对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_add);
        TextView tv_title = findViewById(R.id.tv_title);
        TextView tv_option = findViewById(R.id.tv_option);
        tv_date = findViewById(R.id.tv_date);
        RadioGroup rg_type = findViewById(R.id.rg_type);
        rb_income = findViewById(R.id.rb_income);
        rb_expand = findViewById(R.id.rb_expand);
        et_desc = findViewById(R.id.et_desc);
        et_amount = findViewById(R.id.et_amount);
        tv_title.setText("Please fill in the bill");
        tv_option.setText("List of Bills");
        findViewById(R.id.iv_back).setOnClickListener(this);
        tv_option.setOnClickListener(this);
        tv_date.setOnClickListener(this);
        findViewById(R.id.btn_save).setOnClickListener(this);
        rg_type.setOnCheckedChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        id = getIntent().getIntExtra("id", -1);
        mBillHelper = BillDBHelper.getInstance(this);
        if (id != -1) {
            List<BillInfo> bill_list = (List<BillInfo>) mBillHelper.queryById(id);
            if (bill_list.size() > 0) { // 已存在该账单
                BillInfo bill = bill_list.get(0); // 获取账单信息
                Date date = DateUtil.formatString(bill.date);
                Log.d(TAG, "bill.date="+bill.date);
                Log.d(TAG, "year="+date.getYear()+",month="+date.getMonth()+",day="+date.getDate());
                calendar.set(Calendar.YEAR, date.getYear()+1900);
                calendar.set(Calendar.MONTH, date.getMonth());
                calendar.set(Calendar.DAY_OF_MONTH, date.getDate());
                if (bill.type == 0) {
                    rb_income.setChecked(true);
                } else {
                    rb_expand.setChecked(true);
                }
                et_desc.setText(bill.desc);
                et_amount.setText(""+bill.amount);
            }
        }
        tv_date.setText(DateUtil.getDate(calendar));
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back) {
            finish(); // 关闭当前页面
        } else if (v.getId() == R.id.tv_option) {
            Intent intent = new Intent(this, BillPagerActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //
            startActivity(intent); //
        } else if (v.getId() == R.id.tv_date) {
            DatePickerDialog dialog = new DatePickerDialog(this, this,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        } else if (v.getId() == R.id.btn_save) {
            saveBill();
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        mBillType = (checkedId==R.id.rb_expand) ? 1 : 0;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        tv_date.setText(DateUtil.getDate(calendar));
    }

    // Save the bill
    private void saveBill() {
        //ViewUtil.hideAllInputMethod(this);
        ViewUtil.hideOneInputMethod(this, et_amount);
        BillInfo bill = new BillInfo();
        bill.id = id;
        bill.date = tv_date.getText().toString();
        bill.month = 100*calendar.get(Calendar.YEAR) + (calendar.get(Calendar.MONTH)+1);
        bill.type = mBillType;
        bill.desc = et_desc.getText().toString();
        bill.amount = Double.parseDouble(et_amount.getText().toString());
        mBillHelper.save(bill);
        Toast.makeText(this, "Bill added", Toast.LENGTH_SHORT).show();
        resetPage();
    }

    // Reset View
    private void resetPage() {
        calendar = Calendar.getInstance();
        et_desc.setText("");
        et_amount.setText("");
        tv_date.setText(DateUtil.getDate(calendar));
    }

}