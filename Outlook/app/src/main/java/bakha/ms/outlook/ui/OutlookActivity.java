package bakha.ms.outlook.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import bakha.ms.outlook.data.Account;
import bakha.ms.outlook.data.AccountManager;
import bakha.ms.outlook.data.OutlookManager;
import bakha.ms.outlook.ui.view.AgendaView;
import bakha.ms.outlook.ui.view.CalendarExpandListener;
import bakha.ms.outlook.ui.view.CalendarRecyclerViewAdapter;
import bakha.ms.outlook.ui.view.DateChangeListener;
import bakha.ms.outlook.ui.view.ExpandableView;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_DRAGGING;
import static bakha.ms.outlook.data.OutlookManager.PREVIOUS_3_MONTHS;


public class OutlookActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, CalendarExpandListener, DateChangeListener {

    private static final String TAG = "OutlookActivity";
    private AccountManager mAccountManager;
    private RecyclerView mCalendarView;
    private CalendarRecyclerViewAdapter mCalendarViewAdapter;
    private ExpandableView mExpandableView;
    private OutlookManager mOutlookManager;
    private AgendaView mAgendaView;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_outlook);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mContext = this;
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, AddEventActivity.class);
                intent.putExtra("mode", AddEventActivity.Mode.CREATE);
                mContext.startActivity(intent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        // This method will trigger on item Click of navigation menu
        navigationView.setNavigationItemSelectedListener(this);

        mAccountManager = AccountManager.getInstance();
        initializeAccount();
        mOutlookManager = OutlookManager.getInstance(this);
        addDrawerMenu(navigationView.getMenu());
        initializeViews();
        initializeCalendar();
        initializeAgendaView();
        mOutlookManager.loadEvents();

        if (getIntent().hasExtra("date")) {
            GregorianCalendar dateFromAlarm = (GregorianCalendar) getIntent().getSerializableExtra("date");
            // TODO: after alarm go to the given date directly
        }
        updateTitleBar(OutlookManager.PREVIOUS_3_MONTHS + mOutlookManager.getCurrentWeekDay());
    }

    private void addDrawerMenu(Menu menu) {
        int order = 0;
        List<Account> accounts = mAccountManager.getAccounts();
        for (Account account : accounts) {
            MenuItem menuItem = menu.add(R.id.accounts, account.getAccountId() + order, Menu.NONE, account.getTitle());
            menuItem.setIcon(R.drawable.ic_mail_outline_black_24dp);
            menuItem.setCheckable(true);
            SubMenu subMenu = menu.addSubMenu(R.id.accounts, account.getAccountId() + 100 + order, Menu.NONE, account.getName() + " " + account.getLastName());
            subMenu.add(account.getEmail());
            order ++;
        }
    }

    private void updateTitleBar(int position) {
        String months[] = getResources().getStringArray(R.array.months_in_full);
        Calendar calendar = mOutlookManager.getCalendarDateList().get(position);
        if (calendar.get(Calendar.YEAR) != Calendar.getInstance().get(Calendar.YEAR)) {
            getSupportActionBar().setTitle(months[calendar.get(Calendar.MONTH)] + " " + calendar.get(Calendar.YEAR));
        } else {
            getSupportActionBar().setTitle(months[calendar.get(Calendar.MONTH)]);
        }
    }

    private void initializeViews() {
        mExpandableView = (ExpandableView) findViewById(R.id.expandable_view);
        mExpandableView.setCollapsed(true);
    }

    private void initializeAccount() {
        Account account = new Account();
        account.setAccountId(10000);
        account.setName("John");
        account.setLastName("Smith");
        account.setEmail("bakytnur@hotmail.com");
        account.setTitle("Hotmail Account");
        mAccountManager.addAccount(account);
        mAccountManager.setCurrentAccount(account);

        Account account2 = new Account();
        account2.setAccountId(10020);
        account2.setName("John W");
        account2.setLastName("Smith");
        account2.setEmail("mr.smith@gmail.com");
        account2.setTitle("Gmail Account");
        mAccountManager.addAccount(account2);
    }

    private void initializeCalendar() {
        mCalendarView = (RecyclerView) findViewById(R.id.recycler_view);
        mCalendarViewAdapter = new CalendarRecyclerViewAdapter(mContext, this);
        mCalendarView.setAdapter(mCalendarViewAdapter);
        mCalendarView.setClickable(true);
        mCalendarView.setLayoutManager(new GridLayoutManager(this, 7));
        mCalendarView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case SCROLL_STATE_DRAGGING:
                        if (mExpandableView.isCollapsed()) {
                            mExpandableView.expand();
                        }
                        break;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        scrollCalendarView(PREVIOUS_3_MONTHS + mOutlookManager.getCurrentWeekDay());
    }

    private void initializeAgendaView() {
        mAgendaView = (AgendaView) findViewById(R.id.agenda_view);
        mAgendaView.setDateChangeListener(this);
        mAgendaView.setExpandListener(this);
        scrollAgendaView(PREVIOUS_3_MONTHS + mOutlookManager.getCurrentWeekDay());
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (intent.hasExtra("date")) {
            GregorianCalendar dateFromAlarm = (GregorianCalendar) getIntent().getSerializableExtra("date");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAgendaView != null) {
            mAgendaView.refresh();
        }

        if (mCalendarViewAdapter != null) {
            mCalendarViewAdapter.refresh();
            mCalendarViewAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.outlook, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        // Handle navigation view item clicks here.
        int id = menuItem.getItemId();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        int order = 0;
        for (Account account : mAccountManager.getAccounts()) {
            if (id == (account.getAccountId() + order)) { // main menu
                mAccountManager.setCurrentAccount(account);
                menuItem.setChecked(true);
            } if (id == (account.getAccountId() + (order + 100))) { // submenu
                mAccountManager.setCurrentAccount(account);
                menuItem.setChecked(true);
            } else {
                menuItem.setChecked(false);
            }
            order ++;
        }

        return true;
    }

    @Override
    public void requestCollapse() {
        if (!mExpandableView.isCollapsed()) {
            mExpandableView.collapse();
        }
    }

    @Override
    public void currentDateChanged(int viewId, int position) {
        if (mOutlookManager.getPosition() == position) {
            return;
        }

        updateTitleBar(position);
        if (viewId != R.id.agenda_view) {
            scrollAgendaView(position);
        } else if (viewId == R.id.agenda_view) {
            mCalendarView.scrollToPosition(position);
            mCalendarViewAdapter.notifyDataSetChanged();
        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        mAccountManager.clearAccounts();
        mAccountManager = null;
        mOutlookManager.closeDb();
    }

    private void scrollCalendarView(int position) {
        if (mCalendarView != null) {
            mCalendarView.scrollToPosition(position);
        }
    }

    private void scrollAgendaView(int groupPosition) {
        if (mAgendaView != null) {
            mAgendaView.setSelectedGroup(groupPosition);
        }

    }
}
