package com.inventory_tracker.company_name.eventinventorytracker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.inventory_tracker.company_name.eventinventorytracker.For_Result_Activities.CheckIn;
import com.inventory_tracker.company_name.eventinventorytracker.Utility_Classes.Combo_Context_View;
import com.inventory_tracker.company_name.eventinventorytracker.contentProviders.Event_Content;
import com.inventory_tracker.company_name.eventinventorytracker.detail_fragments.Event;
import com.inventory_tracker.company_name.eventinventorytracker.localDatabase.Amazon_Sync_async;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;

import java.util.List;

/**
 * An activity representing a list of Events. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link EventDetailActivity} representing
 * Item details. On tablets, the activity presents the list of items and
 * Item details side-by-side using two vertical panes.
 */
public class EventListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private EventsAdapter mEventsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.master_event_list);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());


        View recyclerView = findViewById(R.id.event_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        if (findViewById(R.id.event_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setDefaultTab(R.id.tab_events);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                Intent intent = null;
                switch (tabId) {
                    case R.id.tab_add:
                        CharSequence categories[] = new CharSequence[] {"Item", "Accessory", "Bundle", "Event"};

                        AlertDialog.Builder builder = new AlertDialog.Builder(EventListActivity.this);
                        builder.setTitle("Pick a Category");
                        builder.setItems(categories, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = null;
                                switch (which){
                                    case 0:
                                        i = new Intent(EventListActivity.this, com.inventory_tracker.company_name.eventinventorytracker.Create_Activities.Item.class);
                                        break;
                                    case 1:
                                        i = new Intent(EventListActivity.this, com.inventory_tracker.company_name.eventinventorytracker.Create_Activities.Accessory.class);
                                        break;
                                    case 2:
                                        i = new Intent(EventListActivity.this, com.inventory_tracker.company_name.eventinventorytracker.Create_Activities.Item_Bundle.class);
                                        break;
                                    case 3:
                                        i = new Intent(EventListActivity.this, com.inventory_tracker.company_name.eventinventorytracker.Create_Activities.Event.class);
                                        break;
                                    default:
                                        Toast.makeText(EventListActivity.this, "no item", Toast.LENGTH_SHORT).show();
                                }
                                startActivity(i);
                            }
                        });
                        builder.show();
                        break;
                    case R.id.tab_inventory:
                        intent = new Intent(EventListActivity.this, ItemListActivity.class);
                        break;
                    case R.id.tab_events:
                        break;
                }

                if (intent != null) {
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
            }
        });
        bottomBar.setOnTabReselectListener(new OnTabReselectListener() {
            @Override
            public void onTabReSelected(@IdRes int tabId) {
                Intent intent = null;
                switch (tabId) {
                    case R.id.tab_add:
                        CharSequence categories[] = new CharSequence[] {"Item", "Accessory", "Bundle", "Event"};

                        AlertDialog.Builder builder = new AlertDialog.Builder(EventListActivity.this);
                        builder.setTitle("Pick a Category");
                        builder.setItems(categories, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = null;
                                switch (which){
                                    case 0:
                                        i = new Intent(EventListActivity.this, com.inventory_tracker.company_name.eventinventorytracker.Create_Activities.Item.class);
                                        break;
                                    case 1:
                                        i = new Intent(EventListActivity.this, com.inventory_tracker.company_name.eventinventorytracker.Create_Activities.Accessory.class);
                                        break;
                                    case 2:
                                        i = new Intent(EventListActivity.this, com.inventory_tracker.company_name.eventinventorytracker.Create_Activities.Item_Bundle.class);
                                        break;
                                    case 3:
                                        i = new Intent(EventListActivity.this, com.inventory_tracker.company_name.eventinventorytracker.Create_Activities.Event.class);
                                        break;
                                    default:
                                        Toast.makeText(EventListActivity.this, "no item", Toast.LENGTH_SHORT).show();
                                }
                                startActivity(i);
                            }
                        });
                        builder.show();
                        break;
                    case R.id.tab_inventory:
                        break;
                    case R.id.tab_events:
                        intent = new Intent(EventListActivity.this, EventListActivity.class);
                        break;
                }

                if (intent != null) {
                    startActivity(intent);
                    overridePendingTransition(0,0);
                }
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sync:
                new Amazon_Sync_async().execute(new Combo_Context_View(getApplicationContext(),findViewById(R.id.syncProgressBar)));
                break;
            case R.id.help:
                new AlertDialog.Builder(EventListActivity.this)
                        .setTitle("Contact Us")
                        .setMessage(
                                "Brian Miller - Backend Developer (Database)" + "\n" +
                                        "brianmmiller94@gmail.com" + "\n" +
                                        "270-557-6635" + "\n" +
                                        "\n" +
                                        "Tyler Durfey - Frontend Developer (Layout)" + "\n" +
                                        "tylerdurfey94@gmail.com" + "\n" +
                                        "574-249-2351"
                        )
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                            }
                        })
                        .show();

        }

        return true;
    }

    @Override
    public void onResume(){
        super.onResume();
        setupRecyclerView((RecyclerView) findViewById(R.id.event_list));
    }
    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        List<Event_Content.Event> events = Event_Content.get(getApplicationContext()).getEvents();

        if (mEventsAdapter == null) {
            mEventsAdapter = new EventsAdapter(events);
            recyclerView.setAdapter(mEventsAdapter);
        } else {
            mEventsAdapter.setEvents(events);
            mEventsAdapter.notifyDataSetChanged();
        }

    }

    public class EventsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<Event_Content.Event> mEvents;

        public EventsAdapter(List<Event_Content.Event> events) {
            mEvents = events;
        }


        public void setEvents(List<Event_Content.Event> events) {
            mEvents = events;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.content_event_list, parent, false);
            return new EventsHolder(view);
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
            Event_Content.Event event = mEvents.get(position);
            EventsHolder eventsHolder = (EventsHolder) holder;
            eventsHolder.bindEvent(event);

        }

        @Override
        public int getItemCount() {
            return mEvents.size();
        }


    }
    public class EventsHolder extends RecyclerView.ViewHolder{
        public final View mView;

        //Widgets
        private TextView mEventName;

        //Accessory
        private Event_Content.Event mEvent;

        public EventsHolder(View view){
            super(view);
            mView = view;
        }
        public void bindEvent(Event_Content.Event event){
            this.mEvent = event;

            //Init Widgets
            mEventName = (TextView) mView.findViewById(R.id.event_content_EventName_TextView);

            //Set widget properties
            mEventName.setText(mEvent.getName());


            //wire widgets
            mEventName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchEventDetail(mView);
                }
            });
        }
        private void launchEventDetail(View view){
            if (mTwoPane) {
                Bundle arguments = new Bundle();
                arguments.putString(Event.EVENT_ID, mEvent.getId().toString());
                //TODO add argument for two pane to switch between closing the activity or not.
                Event fragment = new Event();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.event_detail_container, fragment)
                        .commit();
            } else {
                Context context = view.getContext();
                Intent intent = new Intent(context, EventDetailActivity.class);
                intent.putExtra(Event.EVENT_ID, mEvent.getId().toString());

                context.startActivity(intent);
            }
        }
    }
}
