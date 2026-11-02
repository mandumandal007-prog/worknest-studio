package com.mandu.worknest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject

private data class Entry(val id: Long, val title: String, val detail: String, val state: String = "Active")
private data class Prefs(val theme: String = "Midnight", val accent: String = "Violet", val currency: String = "₹ INR", val week: String = "Monday", val compact: Boolean = false, val dashboard: Boolean = true, val notifications: Boolean = true, val protect: Boolean = false, val autosave: Boolean = true, val showMoney: Boolean = true)

class MainActivity : ComponentActivity() {
    private val store by lazy { getSharedPreferences("worknest", MODE_PRIVATE) }
    private fun load(key: String): List<Entry> = try { val a = JSONArray(store.getString(key, "[]")); (0 until a.length()).map { val o = a.getJSONObject(it); Entry(o.optLong("id"), o.optString("title"), o.optString("detail"), o.optString("state", "Active")) } } catch (_: Exception) { emptyList() }
    private fun save(key: String, entries: List<Entry>) { val a = JSONArray(); entries.forEach { e -> a.put(JSONObject().put("id",e.id).put("title",e.title).put("detail",e.detail).put("state",e.state)) }; store.edit().putString(key,a.toString()).apply() }
    private fun pref(k: String, d: String) = store.getString(k,d) ?: d
    private fun bool(k: String, d:Boolean) = store.getBoolean(k,d)
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState)
        setContent {
            var projects by remember { mutableStateOf(load("projects")) }; var tasks by remember { mutableStateOf(load("tasks")) }; var clients by remember { mutableStateOf(load("clients")) }; var notes by remember { mutableStateOf(load("notes")) }
            var prefs by remember { mutableStateOf(Prefs(pref("theme","Midnight"),pref("accent","Violet"),pref("currency","₹ INR"),pref("week","Monday"),bool("compact",false),bool("dashboard",true),bool("notifications",true),bool("protect",false),bool("autosave",true),bool("showMoney",true))) }
            fun persistPrefs(p: Prefs) { prefs=p; store.edit().putString("theme",p.theme).putString("accent",p.accent).putString("currency",p.currency).putString("week",p.week).putBoolean("compact",p.compact).putBoolean("dashboard",p.dashboard).putBoolean("notifications",p.notifications).putBoolean("protect",p.protect).putBoolean("autosave",p.autosave).putBoolean("showMoney",p.showMoney).apply() }
            val dark = prefs.theme == "Midnight" || prefs.theme == "OLED"
            val bg = if(dark) Color(0xFF10111A) else Color(0xFFF5F6FA); val panel=if(dark) Color(0xFF1B1D29) else Color.White; val ink=if(dark) Color(0xFFF3F2FA) else Color(0xFF202132); val muted=if(dark) Color(0xFFA7A8B8) else Color(0xFF77798C)
            val accent=when(prefs.accent){"Blue"->Color(0xFF5387FF);"Mint"->Color(0xFF42C6A5);"Orange"->Color(0xFFFF9D58);"Rose"->Color(0xFFE66B91);else->Color(0xFF9B82FF)}
            MaterialTheme(colorScheme=darkColorScheme(primary=accent,background=bg,surface=panel,onSurface=ink) .let { if(dark) it else lightColorScheme(primary=accent,background=bg,surface=panel,onSurface=ink) }) {
                WorknestScreen(projects,tasks,clients,notes,prefs,bg,panel,ink,muted,accent,
                    onProject={projects=it;save("projects",it)},onTask={tasks=it;save("tasks",it)},onClient={clients=it;save("clients",it)},onNote={notes=it;save("notes",it)},onPrefs=::persistPrefs)
            }
        }
    }
}

@Composable private fun WorknestScreen(projects:List<Entry>,tasks:List<Entry>,clients:List<Entry>,notes:List<Entry>,prefs:Prefs,bg:Color,panel:Color,ink:Color,muted:Color,accent:Color,onProject:(List<Entry>)->Unit,onTask:(List<Entry>)->Unit,onClient:(List<Entry>)->Unit,onNote:(List<Entry>)->Unit,onPrefs:(Prefs)->Unit){
    var tab by remember { mutableStateOf("Home") }; var dialog by remember { mutableStateOf("") }; var title by remember { mutableStateOf("") }; var detail by remember { mutableStateOf("") }; var selected by remember { mutableStateOf<Entry?>(null) }; var search by remember { mutableStateOf("") }
    val nav=listOf("Home","Projects","Tasks","Clients","Notes","Settings")
    Column(Modifier.fillMaxSize().background(bg).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal=20.dp, vertical=14.dp),verticalAlignment=Alignment.CenterVertically){ Column(Modifier.weight(1f)){ Text("WORKNEST",color=accent,style=MaterialTheme.typography.labelMedium,fontWeight=FontWeight.Bold); Text(tab,color=ink,style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold) }; Text("●",color=accent); Text("  offline",color=muted,style=MaterialTheme.typography.labelMedium) }
        if(tab=="Home" && prefs.dashboard){ LazyColumn(Modifier.weight(1f),contentPadding=PaddingValues(start=16.dp,end=16.dp,bottom=12.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
            item { Card(colors=CardDefaults.cardColors(containerColor=accent),shape=RoundedCornerShape(24.dp)){ Column(Modifier.padding(20.dp)){ Text("YOUR BUSINESS, IN FLOW",color=Color.White.copy(alpha=.8f),style=MaterialTheme.typography.labelMedium); Text("Make space for\nyour next big thing.",color=Color.White,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold); Spacer(Modifier.height(16.dp)); Text("${projects.size} projects  ·  ${tasks.count{it.state!="Done"}} open tasks",color=Color.White) } } }
            item { Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){ Metric("Projects",projects.size.toString(),panel,ink,muted,Modifier.weight(1f));Metric("Open tasks",tasks.count{it.state!="Done"}.toString(),panel,ink,muted,Modifier.weight(1f));Metric("Clients",clients.size.toString(),panel,ink,muted,Modifier.weight(1f)) } }
            item { SectionTitle("Quick actions",ink); Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf("+ Project","+ Task","+ Note").forEach { label -> OutlinedButton(onClick={dialog=label.removePrefix("+ ")},shape=RoundedCornerShape(16.dp)){Text(label)} } } }
            item { SectionTitle("Focus today",ink) }
            val today=tasks.filter{it.state!="Done"}.take(4); if(today.isEmpty()) item { EmptyCard("A clear runway","Add a task to see your next steps here.",panel,ink,muted) } else items(today){ e->EntryCard(e,panel,ink,muted,accent,{selected=e;dialog="Task"}) }
            item { Text("Everything stays on this device. Export a backup from settings.",color=muted,style=MaterialTheme.typography.bodySmall,modifier=Modifier.padding(vertical=10.dp)) }
        } } else if(tab=="Settings") {
            SettingsPage(prefs,panel,ink,muted,accent,onPrefs)
        } else {
            val (list,kind,cb)=when(tab){"Projects"->Triple(projects,"Project",onProject);"Tasks"->Triple(tasks,"Task",onTask);"Clients"->Triple(clients,"Client",onClient);else->Triple(notes,"Note",onNote)}
            Column(Modifier.weight(1f).padding(horizontal=16.dp)) { OutlinedTextField(search,{search=it},Modifier.fillMaxWidth(),placeholder={Text("Search $tab",color=muted)},singleLine=true,shape=RoundedCornerShape(16.dp)); Spacer(Modifier.height(8.dp)); LazyColumn(verticalArrangement=Arrangement.spacedBy(10.dp),contentPadding=PaddingValues(bottom=12.dp)){ val filtered=list.filter{it.title.contains(search,true)||it.detail.contains(search,true)}; if(filtered.isEmpty()) item { EmptyCard("Your $tab start here","Tap + to add one. Your workspace is private and offline-first.",panel,ink,muted) }; items(filtered){e->EntryCard(e,panel,ink,muted,accent,{selected=e;dialog=kind},if(kind=="Task")({cb(list.map{if(it.id==e.id)it.copy(state=if(it.state=="Done")"Active" else "Done")else it})}) else null)} } }
        }
        Row(Modifier.fillMaxWidth().background(panel).navigationBarsPadding().padding(horizontal=5.dp, vertical=6.dp),horizontalArrangement=Arrangement.SpaceEvenly,verticalAlignment=Alignment.CenterVertically){nav.forEach { n -> Column(Modifier.weight(1f).clickable{tab=n}.padding(vertical=5.dp),horizontalAlignment=Alignment.CenterHorizontally){ Text(if(tab==n)"●" else "○",color=if(tab==n)accent else muted);Text(n,color=if(tab==n)accent else muted,style=MaterialTheme.typography.labelSmall) } } }
    }
    if(dialog.isNotEmpty()) {
        val kind=dialog; val editing=selected; var t by remember(kind,editing){mutableStateOf(editing?.title?:"")}; var d by remember(kind,editing){mutableStateOf(editing?.detail?:"")}; var state by remember(kind,editing){mutableStateOf(editing?.state?:"Active")}
        AlertDialog(onDismissRequest={dialog="";selected=null},containerColor=panel,title={Text(if(editing==null)"New $kind" else "Edit $kind",color=ink)},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){OutlinedTextField(t,{t=it},label={Text("Name / title")},singleLine=true);OutlinedTextField(d,{d=it},label={Text(when(kind){"Project"->"Goal, due date or client";"Task"->"Project, due date or priority";"Client"->"Contact, email or notes";else->"Write a note"})},minLines=2);if(kind=="Task")TextButton(onClick={state=if(state=="Done")"Active" else "Done"}){Text("Status: $state")}}},confirmButton={TextButton(onClick={if(t.isNotBlank()){val item=Entry(editing?.id?:System.currentTimeMillis(),t,d,state); when(kind){"Project"->onProject(if(editing==null)projects+item else projects.map{if(it.id==item.id)item else it});"Task"->onTask(if(editing==null)tasks+item else tasks.map{if(it.id==item.id)item else it});"Client"->onClient(if(editing==null)clients+item else clients.map{if(it.id==item.id)item else it});else->onNote(if(editing==null)notes+item else notes.map{if(it.id==item.id)item else it})};dialog="";selected=null}}){Text("Save")}},dismissButton={Row{if(editing!=null)TextButton(onClick={when(kind){"Project"->onProject(projects.filterNot{it.id==editing.id});"Task"->onTask(tasks.filterNot{it.id==editing.id});"Client"->onClient(clients.filterNot{it.id==editing.id});else->onNote(notes.filterNot{it.id==editing.id})};dialog="";selected=null}){Text("Delete",color=Color(0xFFE76D79))};TextButton(onClick={dialog="";selected=null}){Text("Cancel")}}})
    }
}
@Composable private fun Metric(label:String,value:String,panel:Color,ink:Color,muted:Color,modifier:Modifier=Modifier){Card(modifier,shape=RoundedCornerShape(18.dp),colors=CardDefaults.cardColors(containerColor=panel)){Column(Modifier.padding(12.dp)){Text(value,color=ink,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text(label,color=muted,style=MaterialTheme.typography.labelSmall)}}}
@Composable private fun SectionTitle(text:String,ink:Color){Text(text,color=ink,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold,modifier=Modifier.padding(vertical=3.dp))}
@Composable private fun EntryCard(e:Entry,panel:Color,ink:Color,muted:Color,accent:Color,onClick:()->Unit,onToggle:(()->Unit)?=null){Card(Modifier.fillMaxWidth().clickable(onClick=onClick),shape=RoundedCornerShape(18.dp),colors=CardDefaults.cardColors(containerColor=panel)){Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(38.dp).background(accent.copy(alpha=.15f),CircleShape),contentAlignment=Alignment.Center){Text(e.title.take(1).uppercase(),color=accent,fontWeight=FontWeight.Bold)};Column(Modifier.weight(1f).padding(start=12.dp)){Text(e.title,color=ink,fontWeight=FontWeight.SemiBold);if(e.detail.isNotBlank())Text(e.detail,color=muted,maxLines=2,style=MaterialTheme.typography.bodySmall)};if(onToggle!=null)Checkbox(e.state=="Done",{onToggle()},colors=CheckboxDefaults.colors(checkedColor=accent)) else Text(e.state,color=accent,style=MaterialTheme.typography.labelSmall)}}}
@Composable private fun EmptyCard(title:String,body:String,panel:Color,ink:Color,muted:Color){Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(20.dp),colors=CardDefaults.cardColors(containerColor=panel)){Column(Modifier.padding(20.dp)){Text(title,color=ink,fontWeight=FontWeight.Bold);Text(body,color=muted,style=MaterialTheme.typography.bodyMedium,modifier=Modifier.padding(top=4.dp))}}}
@Composable private fun SettingsPage(p:Prefs,panel:Color,ink:Color,muted:Color,accent:Color,onChange:(Prefs)->Unit){LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(horizontal=16.dp,vertical=8.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
 item{SectionTitle("Appearance",ink)};item{Choice("Theme",listOf("Midnight","OLED","Light"),p.theme,panel,ink,muted){onChange(p.copy(theme=it))}};item{Choice("Accent",listOf("Violet","Blue","Mint","Orange","Rose"),p.accent,panel,ink,muted){onChange(p.copy(accent=it))}};item{Choice("Density",listOf("Comfortable","Compact"),if(p.compact)"Compact" else "Comfortable",panel,ink,muted){onChange(p.copy(compact=it=="Compact"))}}
 item{SectionTitle("Workspace",ink)};item{Choice("Currency",listOf("₹ INR","$ USD","€ EUR","£ GBP"),p.currency,panel,ink,muted){onChange(p.copy(currency=it))}};item{Choice("Week starts",listOf("Monday","Sunday","Saturday"),p.week,panel,ink,muted){onChange(p.copy(week=it))}}
 item{SectionTitle("Dashboard & privacy",ink)};item{ToggleRow("Show dashboard","Hide the overview when you want a focused workspace",p.dashboard,panel,ink,muted){onChange(p.copy(dashboard=it))}};item{ToggleRow("Show money figures","Keep financial amounts visible in workspace",p.showMoney,panel,ink,muted){onChange(p.copy(showMoney=it))}};item{ToggleRow("Notifications","Local task reminder notifications (permission may be needed)",p.notifications,panel,ink,muted){onChange(p.copy(notifications=it))}};item{ToggleRow("Hide app preview","Privacy preference for recent-app previews",p.protect,panel,ink,muted){onChange(p.copy(protect=it))}}
 item{SectionTitle("Data & device",ink)};item{ToggleRow("Auto-save","Changes save locally as you make them",p.autosave,panel,ink,muted){onChange(p.copy(autosave=it))}};item{Card(colors=CardDefaults.cardColors(containerColor=panel),shape=RoundedCornerShape(18.dp)){Column(Modifier.padding(16.dp)){Text("Offline by design",color=ink,fontWeight=FontWeight.Bold);Text("Projects, tasks, clients and notes are stored on this device. No account, cloud sync, payment processing or SaaS backend is connected.",color=muted,style=MaterialTheme.typography.bodySmall,modifier=Modifier.padding(top=5.dp))}}};item{Card(colors=CardDefaults.cardColors(containerColor=panel),shape=RoundedCornerShape(18.dp)){Column(Modifier.padding(16.dp)){Text("Worknest Studio",color=ink,fontWeight=FontWeight.Bold);Text("Version 1.0.0 · Built for a personal workspace · Android 6+ (target API 33)",color=muted,style=MaterialTheme.typography.bodySmall,modifier=Modifier.padding(top=5.dp))}}}
}}
@Composable private fun Choice(label:String,options:List<String>,value:String,panel:Color,ink:Color,muted:Color,onPick:(String)->Unit){Card(colors=CardDefaults.cardColors(containerColor=panel),shape=RoundedCornerShape(18.dp)){Column(Modifier.padding(14.dp)){Text(label,color=ink,fontWeight=FontWeight.SemiBold);Spacer(Modifier.height(8.dp));Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){options.forEach{opt->FilterChip(selected=value==opt,onClick={onPick(opt)},label={Text(opt,style=MaterialTheme.typography.labelSmall)})}}}}}
@Composable private fun ToggleRow(label:String,sub:String,value:Boolean,panel:Color,ink:Color,muted:Color,onToggle:(Boolean)->Unit){Card(colors=CardDefaults.cardColors(containerColor=panel),shape=RoundedCornerShape(18.dp)){Row(Modifier.fillMaxWidth().padding(start=14.dp,end=8.dp,top=8.dp,bottom=8.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(label,color=ink,fontWeight=FontWeight.SemiBold);Text(sub,color=muted,style=MaterialTheme.typography.bodySmall)};Switch(value,onToggle)}}}
