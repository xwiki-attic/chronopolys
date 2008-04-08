var currentContainer;
var currentPage;
var currentSpace;
var currentFullName;
var localUser;
var userName;
var userSpace;
var daySymbol;
var passwdnotmatch;
var passwdtooshort;
var passwdupdated;
var confirmdelphase;
var confirmcontainerdelete;
var confirmplogdelete;
var confirmphasetomilestone;
var colorpickers = [];

// ---------------- //
// Dashboard modes  //
// ---------------- //

var currentMode = "timelinemode";
var currentSecondaryMode = "";
var elementsCache = null;
var elementsAddCache = null;

function toggleContainerAdd() {
    if (elementsAddCache == null) {
        elementsAddCache = document.getElementsByClassName('container_add', $('chronoapp'));
    }
    for (var i = 0; i < elementsAddCache.length; i++) {
        toggleClass(elementsAddCache[i], 'hidden');
    }
    if (!eltHasClass(elementsAddCache[0], 'hidden')) {
        currentSecondaryMode = 'add';
    } else {
        currentSecondaryMode = '';
    }
}

function switchDashboardMode(mode)
{
    // retreive elements related to a mode
    if (elementsCache == null) {
        elementsCache = document.getElementsByClassName('mode', $('chronoapp'));
    }
    // save desired mode
    currentMode = mode;
    // modify visibility of the elements considering their modes
    for (var i = 0; i < elementsCache.length; i++) {
        if (eltHasClass(elementsCache[i], mode)) {
            // desired mode
            if (eltHasClass(elementsCache[i], 'hidden')) {
                rmClass(elementsCache[i], "hidden");
            }
        } else
        {
            // other modes
            if (!eltHasClass(elementsCache[i], 'hidden')) {
                addClass(elementsCache[i], "hidden");
            }
        }
    }
}

// --------- //
// Timelines //
// --------- //

function getVisibleCoords()
{
    var yoff = 0, xoff = 0, wh = 0, ww = 0;
    if (typeof( window.pageYOffset ) == 'number') {
        //Netscape compliant
        yoff = window.pageYOffset;
        xoff = window.pageXOffset;
        wh = window.innerHeight;
        ww = window.innerWidth;
    } else if (document.body && ( document.body.scrollLeft || document.body.scrollTop )) {
        //DOM compliant
        yoff = document.body.scrollTop;
        xoff = document.body.scrollLeft;
        wh = document.body.clientHeight;
        ww = document.body.clientWidth;
    } else if (document.documentElement)
    {
        //IE6 standards compliant mode
        yoff = document.documentElement.scrollTop;
        xoff = document.documentElement.scrollLeft;
        wh = document.documentElement.clientHeight;
        ww = document.documentElement.clientWidth;
    }
    return { xmin: xoff, xmax: ww + xoff, ymin: yoff, ymax: wh + yoff };
}

function isElementHidden(el, pos)
{
    // Modern navigators
    if (pos[0] == 0 && pos[1] == 0) {
        return true;
    }
    // Internet Explorer
    do {
        if (eltHasClass(el, "hidden")) {
            return true;
        }
        el = el.parentNode;
    } while (el);
    return false;
}

function isElementVisible(el)
{
    var pos = Position.cumulativeOffset(el); // get el coords
    if (!isElementHidden(el, pos)) {
        var dcoords = getVisibleCoords(); // get visible coords
        return pos[0] > dcoords.xmin && pos[0] < dcoords.xmax && pos[1] > dcoords.ymin &&
               pos[1] < dcoords.ymax;
    } else
    {
        return false;
    }
}

var g_queue = new Array();
var syncingTimelines = false;

function process_queue()
{
    if (!g_queue.length)
        return;

  // if this code hasn't begun being executed, start 'er up
    if (!g_queue[0][2])
    {
        // run the code
        g_queue[0][0]();
        timeout_id = window.setInterval("check_queue_item_complete()", 50);
        g_queue[0][2] = timeout_id;
    }
}

function check_queue_item_complete()
{
    if (g_queue[0][1]())
    {
        window.clearInterval(g_queue[0][2]);
        g_queue.shift();
        process_queue();
    }
}

var metaTimelines = new Array();
var origTimelines = new Array();

function loadTimelines()
{
    for (var i = 0; i < metaTimelines.length; i++) {
        if (metaTimelines[i].visible && !metaTimelines[i].loaded) {
            var pos = Position.cumulativeOffset(metaTimelines[i].div);
            var dcoords = getVisibleCoords(); // get on-screen visible coords
            /* var info = "x:" + pos[0] + "  y:" + pos[1]
                    + "  xmin:" + dcoords.xmin + "  xmax:" + dcoords.xmax
                    + "  ymin:" + dcoords.ymin + "  ymax:" + dcoords.ymax;
            alert(info); */
            metaTimelines[i].loaded = true;
            g_queue.push([metaTimelines[i].loader, metaTimelines[i].isLoaded]);
        }
    }
    process_queue();
}

function setVisibleTimelines()
{
    for (var i = 0; i < metaTimelines.length; i++) {
        if (isElementVisible(metaTimelines[i].div)) {
            metaTimelines[i].visible = true;
        }
    }
}

function onTimelineVisibilityChange()
{
    setVisibleTimelines();
    loadTimelines();
}

/* Event.observe(window, 'load', onTimelineVisibilityChange, false);
Event.observe(window, 'resize', onTimelineVisibilityChange, false);
Event.observe(window, 'scroll', onTimelineVisibilityChange, false); */

/* add a timeline loading function (and the associated div) to the global list */
function createMetaTimeline(tlload, tldiv, tlname)
{
    var metaTimeline = {
        timeline: null,
        timelineName: tlname,
        loader: function() { this.timeline = tlload(); },
        div: tldiv,
        loaded: false,
        getOrigKey: function () { return this.timelineName },
        isLoaded: function()
        {
            if (this.timeline != null) {
                return this.timeline._isLoaded;
            } else {
                return false;
            }
        },
        visible: false
    }
    metaTimelines.push(metaTimeline);
}

/* draw arrows around the timelines if some events are not shown in the current range */
function drawTimelineArrows(tl)
{
    var tid = tl._containerDiv.id;
    var tlb = tl.getBand(0);
    if (tlb.getEventSource().getCount() > 0) {
        var lbc = $(tid + '_left_img');
        var rbc = $(tid + '_right_img');
        if (tlb.getEventSource().getEarliestDate() < tlb.getMinVisibleDate()) {
            lbc.style.backgroundColor = lbc.alt;
        }
        if (tlb.getEventSource().getLatestDate() > tlb.getMaxVisibleDate()) {
            rbc.style.backgroundColor = rbc.alt;
        }
    }
}

/* save the timeline range in the user profile */
function saveTimelineRange()
{
    var range = $('timeline_range').value;
    var surl = getXWikiURL("XWiki", userName, "save", "");
    var parameters = 'XWiki.XWikiUsers_0_prefered_timeline_view=' + range;
    parameters = parameters + '&comment=savetimelinerange';
    var myAjax = new Ajax.Request(
            surl,
    {
        method: 'post',
        postBody: parameters
    });
}

/* reload the chronocontainers div */
function reloadDashboard(options)
{
    /* avoid all previous timeline datas */
    metaTimelines.clear();
    masterloader = null;
    elementsAddCache = null;

    setLoadingBg('axis_all', true);

    /* update the dashboard */
    var id = 'chronocontainers';
    var surl = getXWikiURL("ChronoServices", "ContainersDisplay", "view", "xpage=plain" + "&mode=" + currentMode + "&modes=" + currentSecondaryMode + options);
    var myAjax = new Ajax.Updater(
            id,
            surl,
    {
        method: 'get',
        evalScripts: true,
        onComplete: function()
        {
            setLoadingBg('axis_all', false);
            /* reload dynamic timelines */
            reloadTimelines();
        }
    });
}

/* timeline's range change */
function changeTimelineRange(range)
{
    reloadDashboard("&prefered_timeline_view=" + range);
}

/* call the timelines loading functions */
function reloadTimelines()
{

    /* the prototype onComplete handler doesn't wait for scripts eval */
    if (masterloader == null) {
        setTimeout("reloadTimelines()", 100);
    }

    /* now that masterloader has been evaluated we can execute it */
    masterloader();

    /* reload the project timelines */
    loadTimelines();

    /* finally save choice in the user profile */
    saveTimelineRange();
}

var syncDate = null;

function syncTimelineRoutine()
{
    if (syncDate) {
        var date = syncDate;

        /* set date on master timeline */
        master_tl.getBand(0).setCenterVisibleDate(date);
        master_tl.getBand(1).setCenterVisibleDate(date);

        /* draw band1 on band0 */
        var startd1 = master_tl.getBand(1).getMinVisibleDate();
        var endd1 = master_tl.getBand(1).getMaxVisibleDate();
        master_tl.getBand(0).getEtherPainter().setHighlight(startd1, endd1);

        /* set date on project timelines */
        for (i = 0; i < metaTimelines.length; i++) {
            if (metaTimelines[i].loaded) {
                var tlb = origTimelines[metaTimelines[i].getOrigKey()].getBand(0);
                var tid = origTimelines[metaTimelines[i].getOrigKey()]._containerDiv.id;
                tlb.setCenterVisibleDate(date);
                if (tlb.getEventSource().getCount() > 0) {
                    var lbc = $(tid + '_left_img');
                    var rbc = $(tid + '_right_img');
                    lbc.style.backgroundColor = 'transparent';
                    rbc.style.backgroundColor = 'transparent';
                    if (tlb.getEventSource().getEarliestDate() < tlb.getMinVisibleDate()) {
                        lbc.style.backgroundColor = lbc.alt;
                    }
                    if (tlb.getEventSource().getLatestDate() > tlb.getMaxVisibleDate()) {
                        rbc.style.backgroundColor = rbc.alt;
                    }
                }
            }
        }
    }

    syncDate = null;

    // recall the method every 2"
    setTimeout("syncTimelineRoutine()", 2000);
}

function timelineStatus(id)
{
    var pos = Position.cumulativeOffset($(id)); // get el coords
    var dcoords = getVisibleCoords(); // get on-screen visible coords
    var info = "x:" + pos[0] + "  y:" + pos[1]
            + "  xmin:" + dcoords.xmin + "  xmax:" + dcoords.xmax
            + "  ymin:" + dcoords.ymin + "  ymax:" + dcoords.ymax;
    // window.status = info;
    // alert(info);
}

Event.observe(window, 'load', syncTimelineRoutine, false);

/* synchronize all the center dates' timelines */
function syncTimeline(date)
{
    syncDate = date;
}

// ------------------ //
// XWiki URL Builders //
// ------------------ //

function getXWikiURL(space, page)
{
    return getXWikIURL(wikiPage, "", "");
}

function getXWikiURL(space, page, mode)
{
    return getXWikIURL(space, page, mode, "");
}

function getXWikiURL(space, page, mode, args)
{
    var surl = "/xwiki/bin/" + mode + "/" + space + "/" + page + "?";
    if (args != "")
        surl = surl + args;
    if (mode == "save") {
        surl += "&ajax=1&xredirect=/xwiki/blank.html";
    }
    return surl;
}

// ----- //
// Utils //
// ----- //

/* get the absolute posion (from top-left corner) of a HTML element, in pixels */
function getAbsolutePos(element)
{
    var SL = 0, ST = 0;
    var is_div = /^div$/i.test(element.tagName);
    if (is_div && element.scrollLeft) {
        SL = element.scrollLeft;
    }
    if (is_div && element.scrollTop) {
        ST = element.scrollTop;
    }
    var r = { x: element.offsetLeft - SL, y: element.offsetTop - ST };
    if (element.offsetParent) {
        var tmp = this.getAbsolutePos(element.offsetParent);
        r.x += tmp.x;
        r.y += tmp.y;
    }
    return r;
}

/* difference between the passed date (dd/mm/yyyy) and today */
function todayDiff(tdate, color)
{
    var today = new Date();
    var ttab = tdate.split("/");
    var year = parseInt(ttab[2], 10);
    var month = parseInt(ttab[1], 10) - 1;
    var day = parseInt(ttab[0], 10);
    var taskday = new Date(year, month, day);
    var one_day = 1000 * 60 * 60 * 24;
    var res = Math.ceil((taskday.getTime() - today.getTime()) / (one_day));
    /*if (res < 0) {
      document.write('<span class="error" style="background-color:white;">');
    }
    document.write(res+daySymbol);
    if (res < 0) {
      document.write('</span>');
      //document.write('<span class="error">'); (span mal ferme - flix)
    }*/

    if (res < 0) {
        document.write('<div class="error"><div class="errorcontent" style="background-color:' +
                       color + '">');
    } else
    {
        document.write('<div class="errorcontent" style="background-color:' + color + '">');
    }
    document.write(res + daySymbol);
    if (res < 0) {
        document.write('</div></div>');
        /*document.write('<span class="error">'); (span mal ferme - flix)*/
    } else
    {
        document.write('</div>');
    }
}

function removeItemFromArray(array, item)
{
    var i = 0;
    while (i < array.length) {
        if (array[i] == item) {
            array.splice(i, 1);
        } else
        {
            i++;
        }
    }
}

// ------------ //
// User profile //
// ------------ //

/* send a post request to authenticate the user (ex: on passwd change) */
function authuser(user, passwd)
{
    var surl = getXWikiURL("XWiki", "XWikiLogin", "loginsubmit", "");
    var parameters = 'j_username=' + user + '&j_password' + passwd;
    var myAjax = new Ajax.Request(
            surl,
    {
        method: 'post',
        postBody: parameters,
        onComplete: function()
        {
        }
    });
}

/* various checks + save of the new password */
function passwdchange()
{
    // var oldpasswd = $('oldpasswd').value;
    $('passwderror').innerHTML = '';
    $('passwdsuccess').innerHTML = '';
    var newpasswd1 = $('newpasswd1').value;
    var newpasswd2 = $('newpasswd2').value;
    if (newpasswd1 != newpasswd2) {
        $('passwderror').innerHTML = passwdnotmatch;
        return;
    }
    if (newpasswd1.length < 4) {
        $('passwderror').innerHTML = passwdtooshort;
        return;
    }
    var surl = getXWikiURL(currentSpace, currentPage, "save", "");
    var parameters = 'XWiki.XWikiUsers_0_password=' + newpasswd1;
    parameters = parameters + '&comment=changepassword'
    var myAjax = new Ajax.Request(
            surl,
    {
        method: 'post',
        postBody: parameters,
        onComplete: function()
        {
            Field.clear('newpasswd1', 'newpasswd2');
            $('passwdsuccess').innerHTML = passwdupdated;
            authuser(userName, newpasswd1);
        }
    });
}

// ---- //
// PLOG //
// ---- //

/* delete any type of plog article */
function deletePlog(space, page) {
    if (confirm(confirmplogdelete)) {
        var surl = getXWikiURL(space, page, "delete", "confirm=1");
        var myAjax = new Ajax.Request(
                surl,
        {
            method: 'post',
            onSuccess: function()
            {
                $('plog_' + space + '.' + page).parentNode.removeChild($($('plog_' + space + '.' + page)));
            }
        });
    }
}

/* called on PLOG article type's change (message,event,task) */
function changeProjectArticleType()
{
    addClass($('task_edit'), 'hidden');
    addClass($('task_completion_edit'), 'hidden');
    addClass($('meeting_dates_edit'), 'hidden');
    addClass($('meeting_location_edit'), 'hidden');
    addClass($('meeting_rsvp_edit'), 'hidden');
    var type = $('ChronoClasses.ProjectArticleClass_0_type').value;
    if (type == "task") {
        rmClass($('task_edit'), 'hidden');
        rmClass($('task_completion_edit'), 'hidden');
    }
    if (type == "meeting") {
        rmClass($('meeting_dates_edit'), 'hidden');
        rmClass($('meeting_location_edit'), 'hidden');
        rmClass($('meeting_rsvp_edit'), 'hidden');
    }
}

/* add a new guest to an event */
function addrsvp()
{
    var user = $('rsvpmember').value;
    setLoadingBg('rsvp', true);
    var id = 'rsvp';
    var surl = getXWikiURL("ChronoServices", "ProjectArticleRsvpDisplay", "view", "xpage=plain&page=" +
                                                                                  currentFullName +
                                                                                  "&action=guestadd&member=" +
                                                                                  user);
    var myAjax = new Ajax.Updater(
            id,
            surl,
    {
        method: 'get',
        onComplete: function() { setLoadingBg('rsvp', false); }
    });
}

/* delete an event's guest */
function delrsvp(rsvpnb)
{
    setLoadingBg('rsvp', true);
    var id = 'rsvp';
    var surl = getXWikiURL("ChronoServices", "ProjectArticleRsvpDisplay", "view", "xpage=plain&page=" +
                                                                                  currentFullName +
                                                                                  "&action=guestdel&rsvpnb=" +
                                                                                  rsvpnb);
    var myAjax = new Ajax.Updater(
            id,
            surl,
    {
        method: 'get',
        onComplete: function() { setLoadingBg('rsvp', false); }
    });
}

/* load the guest entries */
function loadRsvp()
{
    setLoadingBg('rsvp', true);
    var id = 'rsvp';
    var surl = getXWikiURL("ChronoServices", "ProjectArticleRsvpDisplay", "view", "xpage=plain&page=" +
                                                                                  currentFullName);
    var myAjax = new Ajax.Updater(
            id,
            surl,
    {
        method: 'get',
        onComplete: function() { setLoadingBg('rsvp', false); }
    });
}

// ----------------- //
// Project dashboard //
// ----------------- //

function panel_mouseover(id)
{
    rmClass($(id + '_actions'), 'hidden');
}

function panel_mouseout(id)
{
    addClass($(id + '_actions'), 'hidden');
}

function panel_edit(id)
{
    toggleClass($(id + '_edit'), 'hidden');
    toggleClass($(id + '_view'), 'hidden');
}

function panel_refresh(id)
{
    var surl = getXWikiURL("ChronoServices", "ProjectDisplay", "view", "xpage=plain&puid=" +
                                                                       currentSpace + "&type=" +
                                                                       id);
    var myAjax = new Ajax.Updater(
            id,
            surl,
    {
        method: 'get'
    });
}

function panel_save(type)
{
    if (type == "projectdata") {
        projectdata_save();
    }
    if (type == "description") {
        projectdesc_save();
    }
    if (type == "projectphases") {
        projectphases_save();
    }
    if (type == "notifications") {
        notifications_save();
    }
}

function notifications_save()
{
    var id = 'notifications_view';
    $('notifications_content_edit').style.visibility = "hidden";
    var surl = getXWikiURL(currentSpace, currentPage, "view", "");
    parameters = Form.serialize($('notifications_form'));

    var myAjax = new Ajax.Request(
            surl,
    {
        method: 'post',
        postBody: parameters,
        onComplete: function()
        {
            panel_refresh(id);
            toggleClass($('notifications_edit'), 'hidden');
            toggleClass($('notifications_view'), 'hidden');
            $('notifications_content_edit').style.visibility = "visible";
        }
    });
}

function projectdesc_save()
{
    var id = 'description_view';
    $('description_content_edit').style.visibility = "hidden";
    var desc = $('ChronoClasses.ProjectClass_0_desc');
    var surl = getXWikiURL(currentSpace, "WebHome", "save", "");
    var parameters = desc.id + '=' + desc.value;
    parameters = parameters + '&comment=editdescription';

    var myAjax = new Ajax.Request(
            surl,
    {
        method: 'post',
        postBody: parameters,
        onComplete: function()
        {
            panel_refresh(id);
            toggleClass($('description_edit'), 'hidden');
            toggleClass($('description_view'), 'hidden');
            $('description_content_edit').style.visibility = "visible";
        }
    });
}

function projectdata_save()
{
    var reload = false;
    var id = 'projectdata_view';
    $('projectdata_content_edit').style.visibility = "hidden";
    var name = $('ChronoClasses.ProjectClass_0_name');
    var container = $('ChronoClasses.ProjectClass_0_container');
    if (container.value != currentContainer) {
        reload = true;
    }
    var codename = $('ChronoClasses.ProjectClass_0_codename');
    var status = $('ChronoClasses.ProjectClass_0_status');
    /* var start = $('ChronoClasses.ProjectClass_0_start');
    var end = $('ChronoClasses.ProjectClass_0_end'); */
    var surl = getXWikiURL(currentSpace, "WebHome", "save", "");
    var parameters = name.id + '=' + name.value;
    parameters = parameters + '&' + container.id + '=' + container.value;
    parameters = parameters + '&' + codename.id + '=' + codename.value;
    parameters = parameters + '&' + status.id + '=' + status.value;
    /* parameters = parameters + '&' + start.id + '=' + start.value;
    parameters = parameters + '&' + end.id + '=' + end.value; */
    parameters = parameters + '&comment=editprojectinfos';

    var myAjax = new Ajax.Request(
            surl,
    {
        method: 'post',
        postBody: parameters,
        onComplete: function()
        {
            if (reload) {
                window.location.reload(false);
            } else
            {
                panel_refresh(id);
                toggleClass($('projectdata_edit'), 'hidden');
                toggleClass($('projectdata_view'), 'hidden');
                $('projectdata_content_edit').style.visibility = "visible";
            }
        }
    });
}

// ---------------- //
// Phases calendars //
// ---------------- //

var phasesList = new Array();
var regDatesList = new Array();
var secsPerDay = 86400000;

function calInitDates()
{
    regDatesList.clear();
    var regDatesElts = document.getElementsByClassName('registereddate', $('projectphases_edit'));
    /* build the phases dates list */
    for (var i = 0; i < regDatesElts.length; i++) {
        var pushit = true
        var phaseNb = regDatesElts[i].id.split("_")[1];
        var phaseTypeId = 'ChronoClasses.ProjectPhaseClass_' + phaseNb + '_type';
        if ($(phaseTypeId).value == '1') {
            /* the current date is from a milestone */
            if (regDatesElts[i].id.match(/start/)) {
                /* set the end date in case of 'milestone' */
                $(regDatesElts[i + 1].id).value = $(regDatesElts[i].id).value;
            }
            // } else
            // {
                /* don't push the milestone end in the list */
                //pushit = false;
            // }
        }
        if (pushit) {
            /* push current date into the list */
            var data = new Array();
            data.push(regDatesElts[i].id);
            var sdate = regDatesElts[i].value.split("/");
            data.push(new Date(sdate[2], parseInt(sdate[1], 10) - 1, sdate[0]));
            regDatesList.push(data);
        }
    }

    /* display (or not) the "add phase here" button */
    for (var i = 0; i < regDatesList.length - 1; i++) {
        var evalit = true;
        var phaseNb = regDatesList[i][0].split("_")[1];
        /* display the addphase button if available */
        var phaseTypeId = 'ChronoClasses.ProjectPhaseClass_' + phaseNb + '_type';
        // if ($(phaseTypeId).value == '1') {
            // if (!regDatesList[i][0].match(/start/))
               // evalit = false;
        // } else
        // {
            if (!regDatesList[i][0].match(/end/))
                evalit = false;
        // }
        if (evalit) {
            var phaseAddElId = 'phaseedition_' + phaseNb + '_addel';
            var phaseAddTdId = 'phaseedition_' + phaseNb + '_addtd';
            if (((regDatesList[i + 1][1] - regDatesList[i][1]) / secsPerDay) > 1) {
                /* if the difference between Nend and N+1start > 1 day, display addphase button */
                rmClass($(phaseAddElId), 'hidden');
                new Effect.Highlight($(phaseAddTdId),
                { startcolor:'#F8EA95',
                    endcolor:'#EFEFEF',
                    duration: 1
                });
            } else
            {
                /* else hide addphase button */
                addClass($(phaseAddElId), 'hidden');
            }
        }
    }

    /* hide the "add phase" button of the last phase if any */
    if(regDatesList.length >= 1)
    {
        var phaseNb = regDatesList[regDatesList.length - 1][0].split("_")[1];
        var phaseAddElId = 'phaseedition_' + phaseNb + '_addel';
        if (!eltHasClass($(phaseAddElId), 'hidden')) {
            addClass($(phaseAddElId), 'hidden');
        }
    }
}

function projectphases_save()
{
    setLoadingBg('projectphases_edit', true);
    var id = 'projectphases_view';
    $('projectphases_content_edit').style.visibility = "hidden";
    var surl = getXWikiURL(currentSpace, "ProjectPhases", "save", "");
    parameters = Form.serialize($('projectphases_form'));
    parameters = parameters + '&comment=editphases';

    var myAjax = new Ajax.Request(
            surl,
    {
        method: 'post',
        postBody: parameters,
        onComplete: function()
        {
            panel_refresh(id);
            toggleClass($('projectphases_edit'), 'hidden');
            $('projectphases_content_edit').style.visibility = "visible";
            setLoadingBg('projectphases_edit', false);
            toggleClass($('projectphases_view'), 'hidden');
        }
    });
}

function registerPhase(id)
{
    phasesList.push(id);
}

function phaseTypeOnChange(phasenb)
{
    if (!confirm(confirmphasetomilestone)) {
        $('ChronoClasses.ProjectPhaseClass_' + phasenb + '_type').selectedIndex = 0;
        return;
    }
    var type = $('ChronoClasses.ProjectPhaseClass_' + phasenb + '_type').value;
    if (type == "1") {
        addClass($('phaseedition_' + phasenb + '_start'), 'hidden');
        addClass($('phaseedition_' + phasenb + '_end'), 'hidden');
        addClass($('phaseedition_' + phasenb + '_endd'), 'hidden');
        addClass($(phasenb + '_currentcontainer'), 'hidden');
        $('ChronoClasses.ProjectPhaseClass_' + phasenb + '_type').disabled = 'disabled';
        rmClass($('phaseedition_' + phasenb + '_mile'), 'hidden');
        $('ChronoClasses.ProjectPhaseClass_' + phasenb + '_iscurrentphase').value = '0';
        $('ChronoClasses.ProjectPhaseClass_' + phasenb + '_end').value = $('ChronoClasses.ProjectPhaseClass_' + phasenb + '_start').value;
        calInitDates();
    } else
    {
        // useless since the way back is forbidden
        rmClass($('phaseedition_' + phasenb + '_start'), 'hidden');
        rmClass($('phaseedition_' + phasenb + '_end'), 'hidden');
        rmClass($('phaseedition_' + phasenb + '_endd'), 'hidden');
        addClass($('phaseedition_' + phasenb + '_mile'), 'hidden');
    }
    calInitDates();
}

function switchPhase(phaseid)
{
    var inputid = 'ChronoClasses.ProjectPhaseClass_' + phaseid + '_iscurrentphase';
    var lightid = phaseid + '_iscurrent';
    var textid = phaseid + '_iscurrent_text';
    var nottextid = phaseid + '_notcurrent_text';
    if ($(inputid).value == "0") {
        /* switch all phases to notcurrent */
        for (var i = 0; i < phasesList.length; i++) {
            var tid = 'ChronoClasses.ProjectPhaseClass_' + phasesList[i] + '_iscurrentphase';
            var lighttid = phasesList[i] + '_iscurrent';
            var texttid = phasesList[i] + '_iscurrent_text';
            var nottexttid = phasesList[i] + '_notcurrent_text';
            if ($(tid).value != "0") {
                $(tid).value = "0";
                /* switch off the corresponding lighbulbs */
                addClass($(lighttid), 'invisible');
                addClass($(texttid), 'hidden');
                rmClass($(nottexttid), 'hidden');
            }
        }
        /* swith the calling phase to active */
        $(inputid).value = "1";
        rmClass($(lightid), 'invisible');
        rmClass($(textid), 'hidden');
        addClass($(nottextid), 'hidden');
    } else
    {
        /* switch the calling phase to inactive */
        $(inputid).value = "0";
        addClass($(lightid), 'invisible');
    }
}

function addphase(elderbrother, after)
{
    setLoadingBg('projectphases_edit', true);
    var surl = getXWikiURL("ChronoServices", "ProjectDisplay", "view", "");
    var parameters = "xpage=plain&type=projectphases_add&";
    parameters += "puid=" + currentSpace + "&";

    var i;
    var date1 = new Date();
    var date2 = new Date();
    if (regDatesList.length > 0) {
        date1.setTime(regDatesList[regDatesList.length - 1][1].getTime());
    }
    date2.setTime(date1.getTime() + (30 * secsPerDay));
    /* push index (i) to the previous phase's start */
    for (i = 0; i < regDatesList.length; i++) {
        if (regDatesList[i][0].split('_')[1] == elderbrother.split('_')[1]) {
            /* get end date of previous phase */
            date1 = regDatesList[i + 1][1];
            if (regDatesList[i + 2][1]) {
                /* get start date of next phase if any */
                date2 = regDatesList[i + 2][1];
            }
            break;
        }
    }

    // useless since XWIKI-1545
    // var pdate1 = new Date(date1.getTime() + (secsPerDay + secsPerDay / 2));
    // var pdate2 = new Date(date2.getTime() - (secsPerDay + secsPerDay / 2));
    var pdate1 = new Date(date1.getTime() + secsPerDay);
    var pdate2 = new Date(date2.getTime() - secsPerDay);
    parameters += "start=" + pdate1.getTime() + "&";
    parameters += "end=" + pdate2.getTime();

    var myAjax = new Ajax.Request(
            surl,
    {
        method: 'post',
        postBody: parameters,
        onComplete: function(transport)
        {
            panel_refresh('projectphases_view');
            /* add edition TR */
            if (after)
                new Insertion.After(elderbrother, transport.responseText);
            else
                new Insertion.Before(elderbrother, transport.responseText);
            /* reinit calendars */
            calInitDates();
            /* update XObject number */
            $('ChronoClasses.ProjectPhaseClass_nb').value = regDatesList.length / 2;
            setLoadingBg('projectphases_edit', false);
        }
    });
}

function deletephase(phasenb)
{
    var ret = confirm(confirmdelphase);
    if (!ret) {
        return;
    }
    setLoadingBg('projectphases_edit', true);
    var id = 'projectphases_view';
    var surl = getXWikiURL(currentSpace, "ProjectPhases", "objectremove", "");
    parameters = "classname=ChronoClasses.ProjectPhaseClass&classid=" + phasenb;
    parameters = parameters + '&comment=deletephase';
    var myAjax = new Ajax.Request(
            surl,
    {
        method: 'post',
        postBody: parameters,
        onComplete: function()
        {
            /* remove phase from phaseList */
            removeItemFromArray(phasesList, phasenb);
            /* update phases view */
            panel_refresh(id);
            /* update XObject number */
            $('ChronoClasses.ProjectPhaseClass_nb').value = regDatesList.length / 2;
            $('phaseedition_' + phasenb + '_add').parentNode.removeChild($('phaseedition_' +
                                                                           phasenb + '_add'));
            /* remove phase edition from DOM */
            $('phaseedition_' + phasenb).parentNode.removeChild($('phaseedition_' + phasenb));
            /* refresh phases dates list */
            calInitDates();

            setLoadingBg('projectphases_edit', false);
        }
    });
}

function change_article_type()
{
    var type = $('article_type_choice').value;
    var elementList = document.getElementsByClassName('article');
    var elements = $A(elementList);
    if (type == '') {
        elements.each(function(element)
        {
            if (eltHasClass(element, 'hidden')) {
                rmClass(element, 'hidden');
            }
        });
    } else
    {
        elements.each(function(element)
        {
            if (!eltHasClass(element, 'hidden')) {
                addClass(element, 'hidden');
            }
        });
        elementList = document.getElementsByClassName(type);
        elements = $A(elementList);
        elements.each(function(element)
        {
            if (eltHasClass(element, 'hidden')) {
                rmClass(element, 'hidden');
            }
        });
    }
}

// ------------------ //
// Projects dashboard //
// ------------------ //

function refresh_container_stateicon(id)
{
    if (eltHasClass($(id), "hidden")) {
        var img = $(id.split('_')[0] + '_openCloseImg');
        if (img)
            if (!eltHasClass($(id.split('_')[0] + '_content'), "activity"))
                img.src = '/xwiki/skins/chronopolys/open.gif';
            else
                img.src = '/xwiki/skins/chronopolys/plus.gif';
    } else
    {
        var img = $(id.split('_')[0] + '_openCloseImg');
        if (img)
            if (!eltHasClass($(id.split('_')[0] + '_content'), "activity"))
                img.src = '/xwiki/skins/chronopolys/close.gif';
            else
                img.src = '/xwiki/skins/chronopolys/minus.gif';
    }
}

function childs_visibility(id, type)
{
    toggleClass($(id), 'hidden');
    refresh_container_stateicon(id);
    setTimeout("onTimelineVisibilityChange()", 1000);
}

function container_mouseout(id)
{
    addClass($(id + '_actions'), 'hidden');
}

function container_mouseover(id)
{
    rmClass($(id + '_actions'), 'hidden');
}

function setLoadingBg(id, state)
{
    if (state == true) {
        var pos = Position.cumulativeOffset($(id)); // get el coords
        var dim = Element.getDimensions(id);
        $('loading').style.left = pos[0] + "px";
        $('loading').style.top = pos[1] + "px";
        $('loading').style.width = dim.width + "px";
        $('loading').style.height = dim.height + "px";
    } else
    {
        $('loading').style.left = "0px";
        $('loading').style.top = "0px";
        $('loading').style.width = "0px";
        $('loading').style.height = "0px";
    }
}

function container_add(type, parent)
{
    var surl = getXWikiURL("ChronoServices", "ContainerDisplay", "view", "xpage=plain&action=add&type=" +
                                                                         type + "&parent=" +
                                                                         parent);
    var containerdiv = parent;
    if (containerdiv == '') {
      containerdiv = 'axis_all';
    }
    setLoadingBg(containerdiv, true);
    var myAjax = new Ajax.Request(
            surl,
    {
        method: 'get',
        onSuccess: function(transport) {
            var params = '&modes=edit' + transport.responseText + '&type=' + type;
            container_refresh_action(parent, params);      
        }
    });
}

function container_save(id)
{
    var name = '';
    var style = '';
    var desc = '';

    if ($(id + '_input_name')) // handle missing el
        name = $(id + '_input_name').firstChild;
    if ($(id + '_input_style')) // handle missing el
        var style = $(id + '_input_style').firstChild;
    if ($(id + '_input_desc').firstChild) // handle missing el
        desc = $(id + '_input_desc').firstChild;
    pid = container_get_parent_id(id);
    setLoadingBg(pid, true);
    var surl = getXWikiURL("ProjectContainers", id, "save", "");
    var parameters = name.id + '=' + name.value + '&' + style.name + '=' + style.value + '&' +
                     desc.id + '=' + desc.value;
    var myAjax = new Ajax.Request(
            surl,
    {
        method: 'post',
        postBody: parameters,
        onComplete: function()
        {
            if($(id).className.indexOf("activity") != -1) container_refresh_action(pid, "type=activity");
            else container_refresh_action(pid, "");
        }
    });
}

function container_refresh_action(id, params) {
    elementsAddCache = null;

    /* top level folder */
    if (id == '' || id == 'axis_all') {
        reloadDashboard(params);
        return;
    }

    /* do not post preferences to user profile, update running! */
    isUpdatingContainers = true;

    /* get the desired container */
    var surl = getXWikiURL("ChronoServices", "ContainerDisplay", "view", "xpage=xpart&vm=plain.vm&pcuid=" + id + "&mode=" + currentMode + "&modes=" + currentSecondaryMode + "&seed=" + Math.floor(Math.random()*10000) + "&" + params);
    var myAjax = new Ajax.Updater (
      id,
      surl,
      {
        method: 'get',
        evalScripts: true,
        onComplete: function() {
          setLoadingBg(id, false);
          if(params.indexOf('type=activity') >= 0) {
            var childsid = id + "_childs";
            childs_visibility(childsid, 'yard');
          }
          else  {
            $(id).style.height = '';
          }
          // re-enable the visibility preference save
          isUpdatingContainers = false;
          // flush the hiddenable elements cache
          elementsCache = null;
        }
      });
}

function container_get_parent_id(id) {
    var pid = $(id).parentNode.parentNode.parentNode.id;
    if (pid == "chronoapp") {
      pid = 'axis_all';
    }
    return pid;
}

function container_move(id, way)
{
    var pid = container_get_parent_id(id);
    setLoadingBg(pid, true);
    var surl = getXWikiURL("ChronoServices", "ContainerDisplay", "view", "xpage=plain&action=" +
                                                                         way + "&pcuid=" + id);
    var myAjax = new Ajax.Request(
            surl,
    {
        method: 'get',
        onComplete: function(transport)
        {
            container_refresh_action(pid);
        }
    });
}

function container_delete_action(id)
{
    var surl = getXWikiURL("ChronoServices", "ContainerDisplay", "view", "pcuid=" + id +
                                                                         "&action=delete");
    var pid = container_get_parent_id(id);
    setLoadingBg(pid, true);
    var myAjax = new Ajax.Request(
            surl,
    {
        method: 'get',
        onSuccess: function()
        {
            if($(id).className.indexOf("activity") != -1) container_refresh_action(pid, "type=activity");
            else container_refresh_action(pid, "");
        }
    });
}

function container_infos(id)
{
    toggleClass($(id + '_infos'), "hidden");
}

function container_delete(id)
{
    if (confirm(confirmcontainerdelete)) {
        container_delete_action(id);
    }
}

function container_edit(id)
{
    toggleClass($(id + '_edit'), "hidden");
}

function blindup(id)
{
    Effect.BlindUp(id, { queue: 'end' })
}

var visibleContainers;
var visibleEls = null;
var updatingContainers = false;

function isUpdatingContainers(state) {
  updatingContainers = state;
}

function getContainersStates()
{
    if (visibleEls == null || visibleEls.length == 0) {
        visibleEls = document.getElementsByClassName('hiddenable', $('axis_all'));
    }
    var elist = new Array();
    var str = '[';
    for (var i = 0; i < visibleEls.length; i++) {
        if (!eltHasClass(visibleEls[i], 'hidden')) {
            if (elist.length > 0) {
                str += ',';
            }
            elist.push(visibleEls[i].id);
            str += '"' + visibleEls[i].id + '"';
        }
    }
    str += ']';
    return str;
}

function saveContainersStates()
{
    var newhc = getContainersStates();
    if (newhc != visibleContainers && !updatingContainers) {
        visibleContainers = newhc;
        var surl = getXWikiURL(userSpace, userName, "save", "");
        var parameters = 'XWiki.XWikiUsers_0_prefered_containers_state=' + visibleContainers;
        var myAjax = new Ajax.Request(
                surl,
        {
            method: 'post',
            postBody: parameters
        });
    }
    // recall the method every 10"
    setTimeout("saveContainersStates()", 10000);
}

function initContainersStates(visibleContainers)
{
    if (visibleContainers != '') {
        var visibleContainersTab = eval(visibleContainers);
    } else
    {
        visibleContainersTab = new Array();
    }
    for (var i = 0; i < visibleContainersTab.length; i++) {
        if ($(visibleContainersTab[i])) {
	    if (eltHasClass($(visibleContainersTab[i]), 'hidden')) {
                rmClass($(visibleContainersTab[i]), 'hidden');
            }
            refresh_container_stateicon(visibleContainersTab[i]);
        }
    }
    // first update call 15" after loading
    setTimeout("saveContainersStates()", 15000);
}

function processInfoBubble(id, uid)
{
    var myDiv = $(id);
    var coords = getAbsolutePos(myDiv);
    coords.x = coords.x + 8;
    coords.y = coords.y + 2;
    var bubble = Timeline.Graphics.createBubbleForPoint(window.document, coords.x, coords.y, 300, 100);
    bubble.content.innerHTML = $(uid + '_input_desc').firstChild.value;
}

function switchClass(id)
{
    target = document.getElementById(id);
    if (target.className == "article_deploy_closed")
        target.className = "article_deploy_open";
    else if (target.className == "article_deploy_open")
        target.className = "article_deploy_closed";
}


// function to delete a user from Administration
function deleteUser(id, fullname)
{
    if(confirm(confirmdeleteuser)) {
        var userspace = fullname.substring(0, fullname.indexOf('.'));
        var userpage = fullname.substring(fullname.indexOf('.') + 1);
        var url = getXWikiURL(userspace, userpage, "delete", "confirm=1");
        new Ajax.Request(url, {
            method: 'get',
            onSuccess: function()
            {
                $(id).parentNode.removeChild($(id));
            }
        });
    }
}


function addNote(note, docweb, docname) {
      var id = 'projectnote';
      var options = "&do=note&note=" + note;
      var surl = getXWikiURL(docweb, docname, "view", "xpage=plain" + options);
      var myAjax = new Ajax.Updater(
        id,
        surl,
        {
          method: 'get',
          evalScripts: true
        });
    }

function setdisplayednote(note) {
      for (var i = 1; i <= 5; i++) {
        var sobj = $('note-star-'+i);
        if (i > note) {
          if (!eltHasClass(sobj, "empty"))
            addClass(sobj, "empty");
        } else {
          if (eltHasClass(sobj, "empty"))
            rmClass(sobj, "empty");
        }
      }
}

 function initstars(initialnote) {
      setdisplayednote(initialnote);
 }

 // find top, left positions cross-browser
 function findPos(obj) {
	var curleft = curtop = 0;
	if (obj.offsetParent) {
		do {
			curleft += obj.offsetLeft;
			curtop += obj.offsetTop;
		} while (obj = obj.offsetParent);
	}
	return [curleft,curtop];
}

 /*---------------------------------------------------------------------------------//
 // Slider                              
    @param domNode The div that represents the slider
    @param trackNode The div that represents the slider track
    @param slideNo The number use to distinguish from multiple sliders on the same page
    @param updCol The slider might have to update the color of the track
    @param loading The div used to show ajax request in progress
    @param updHandler The function used to update several elements when slider moves
    @param url The url used for ajax request
    @param maxVal The maximum value the slider can display (ex. 100)
 //---------------------------------------------------------------------------------*/
 
xwkSlider = Class.create();

xwkSlider.prototype = {
  initialize: function(params)
  {
    this.domNode = $(params.domNode);
    this.trackNode = $(params.trackNode); 
    this.slideNo = ('slideNo' in params ? params.slideNo : 0);
    this.updCol = ('updCol' in params ? $(params.updCol) : null);
    this.loading = $(params.loading);
    this.updHandler = ('updHandler' in params ? params.updHandler : function() { }  );
    this.step = ('step' in params ? params.step : 10); 
    this.maxVal = ('maxVal' in params ? params.maxVal : 100); 
    this.url = params.url;
    this.dist = this.trackNode.clientWidth;
    this.start = findPos(this.trackNode)[0];
    this.mouseDown = false;
    this.keyDown = false;
    this.temp = -1;
    this.attachEventHandlers();
  },
  attachEventHandlers: function()
  {
      this.mouseUpHandler = this.makeMouseUpHandler(this);
      this.mouseMoveHandler = this.makeMouseMoveHandler(this);
      this.keyUpHandler = this.makeKeyUpHandler(this);
      Event.observe(this.domNode, 'mousedown', this.makeMouseDownHandler(this));
      Event.observe(this.trackNode, 'mouseover', this.makeMouseOverHandler(this));
      Event.observe(this.trackNode, 'mouseout', this.makeMouseOutHandler(this));
      Event.observe(this.domNode, 'mouseover', this.makeMouseOverHandler(this));
      Event.observe(this.domNode, 'mouseout', this.makeMouseOutHandler(this));
      Event.observe(document, 'keydown', this.makeKeyDownHandler(this));
  },
  makeKeyDownHandler: function(self)
  {
    return function(ev) {
      if(window.activeSlider != self) return;
      var ev = (!ev) ? window.event : ev;
      self.keyDown = true;
      Event.observe(document, 'keyup', self.keyUpHandler);
      if (ev.keyCode) code = ev.keyCode;
      else if (ev.which) code = ev.which;
      if(self.dist == 0 || self.start == 0) {
          self.dist = window.dist;
          self.start = window.start;
      }
      if(code == 39) // right arrow key
      { 
        self.tempdp = parseInt(self.domNode.style.left); // the percent at key down
        self.dp = self.tempdp;
        self.dp += self.step; // computing the new percent
        if(self.dp > self.maxVal) self.dp = self.maxVal;
        self.domNode.style.left = self.dp + "%"; // updating in real time
        if(self.updCol) self.updCol.style.width = self.dp + "%";
        self.updHandler(self.slideNo, self.dp, self.tempdp);
      }
      else if(code == 37) //left arrow key
      { 
        self.tempdp = parseInt(self.domNode.style.left);
        self.dp = self.tempdp;
        self.dp -= self.step;
        if(self.dp < 0) self.dp = 0;
        self.domNode.style.left = self.dp + "%";
        if(self.updCol) self.updCol.style.width = self.dp + "%";
        self.updHandler(self.slideNo, self.dp, self.tempdp);
      }
    }
  },
  
  makeKeyUpHandler: function(self)
  {
    return function(ev) {
      var ev = (!ev) ? window.event : ev;
      if (ev.keyCode) code = ev.keyCode;
      else if (ev.which) code = ev.which;
      // verify if the percent to change is different than previous percent
      // to avoid making ajax request for the same value
      if((code == 37 || code == 39) && self.keyDown && self.temp != self.dp) {
          var url = self.url;
          if(url.indexOf("?taskcompletion") > 0) 
            url = self.url.substring(0, self.url.indexOf("?taskcompletion"));
          url += "?taskcompletion=" + self.dp + "%";
          var pivot = self;
          self.loading.style.display = "block";
          new Ajax.Request(url, {
              method: 'get',
              onSuccess: function(transport)  {
                pivot.loading.style.display = "none";
                pivot.temp = parseInt(pivot.domNode.style.left);
              },
              onFailure: function(transport) {
                // restore the previous percent
                pivot.domNode.style.left = pivot.tempdp + "%"; 
                if(pivot.updCol) pivot.updCol.style.width = pivot.tempdp + "%";
                pivot.updHandler(pivot.slideNo, pivot.dp, pivot.tempdp);
              }
          });
      }
      self.keyDown = false;
      Event.stopObserving(document, 'keyup', self.keyUpHandler);
    }
  },
  makeMouseOverHandler: function(self)
  {
    return function(ev) {
      window.activeSlider = self;
    }
  },
  makeMouseOutHandler: function(self)
  {
    return function(ev) {
      window.activeSlider = null;
    }
  },
  makeMouseDownHandler: function(self)
  {
    return function(ev) {
      var ev = (!ev) ? window.event : ev;
      if(self.dist == 0 || self.start == 0) {
          self.dist = window.dist;
          self.start = window.start;
      }
      self.mouseDown = true; 
      self.evX = ev.screenX;
      self.tempdp = parseInt(self.domNode.style.left);
      Event.observe(document, 'mouseup', self.mouseUpHandler);
      Event.observe(document, 'mousemove', self.mouseMoveHandler);
    }
  },
  makeMouseMoveHandler: function(self) {
    return function(ev) {
      var ev = (!ev) ? window.event : ev;
      if(self.mouseDown) 
      {
        self.x = ev.screenX - self.start;
        if(self.evX > ev.screenX) // drag left
          self.x -= (self.domNode.clientWidth > 0 ? self.domNode.clientWidth : 10);  //QUICKFIX: change here if the slider handler width changes    
        if(self.x < 0) self.x = 0;
        if(self.x > self.dist) self.x = self.dist;
        self.domNode.style.left = self.x + "px";
        if(self.updCol) self.updCol.style.width = self.x + "px";
        self.p = Math.round(self.x * self.step / self.dist);
        self.dp = self.p * self.step; // percent
        if(self.dp < 0) self.dp = 0;
        if(self.dp > self.maxVal) self.dp = self.maxVal;
        self.updHandler(self.slideNo, self.dp, self.tempdp);
        Event.stop(ev);
      }
    }
  },
  makeMouseUpHandler: function(self) {
    return function(ev) {
      var ev = (!ev) ? window.event : ev;
      if(self.mouseDown) 
      { 
          //snapping
          self.p = Math.round(self.x * self.step / self.dist);
          self.x = (self.p * self.dist) / self.step; // pixels
          self.dp = self.p * self.step; // percent
          if(self.dp < 0) self.dp = 0;
          if(self.dp > self.maxVal) self.dp = self.maxVal;
          self.domNode.style.left = self.dp + "%";
          if(self.updCol) self.updCol.style.width = self.dp + "%";         
          self.updHandler(self.slideNo, self.dp, self.tempdp);       
          var url = self.url;
          if(url.indexOf("?taskcompletion") > 0) 
            url = self.url.substring(0, self.url.indexOf("?taskcompletion"));
          url += "?taskcompletion=" + self.dp + "%";
          var pivot = self;
          self.loading.style.display = "block";
          new Ajax.Request(url, {
              method: 'get',
              onSuccess: function(transport) {
                pivot.loading.style.display = "none";
                pivot.tempdp = pivot.dp;
              },
              onFailure: function(transport) {
                pivot.domNode.style.left = pivot.tempdp + "%";
                if(pivot.updCol) pivot.updCol.style.width = pivot.tempdp + "%";
                pivot.updHandler(pivot.slideNo, pivot.tempdp, -1);
              }
          });
      Event.stopObserving(document, 'mouseup', self.mouseUpHandler);
      Event.stopObserving(document, 'mousemove', self.mouseMoveHandler);
     }
      self.mouseDown = false;
    }
  }
}

function updTaskCompl(no, per, oldper) 
{
  var id1 =  id2 = id3 = id4 = "";
  if(no == 0)
  {
    id1 = 'task_completed_value';
    id2 = 'plogheaderleft';
    id3 = 'plogtype';
    id4 = 'plogheaderimg';
  }
 else
 {
   id1 = 'task_completed_value_' + no;
   id2 = 'plogheaderleft_' + no;
   id3 = 'plogtype_' + no;
   id4 = 'plogheaderimg_' + no;
 }
  if(per != oldper) {
    if($(id1)) $(id1).innerHTML = per + "%";
    if(per == 100) {
      if($(id2)) $(id2).innerHTML = taskcomplete;
      if($(id3)) $(id3).innerHTML = taskcomplete;
      if($(id4)) $(id4).src = taskcompleteimg;
    }
    else if(oldper == 100) {
      if($(id2)) $(id2).innerHTML = task;
      if($(id3)) $(id3).innerHTML = task;
      if($(id4)) $(id4).src = taskimg;
    }
    else if(oldper == -1) {
      if(per == 100) {
        if($(id2)) $(id2).innerHTML = taskcomplete;
        if($(id3)) $(id3).innerHTML = taskcomplete;
        if($(id4)) $(id4).src = taskcompleteimg;
      }
      else {
        if($(id2)) $(id2).innerHTML = task;
        if($(id3)) $(id3).innerHTML = task;
        if($(id4)) $(id4).src = taskimg;
      }
    }
  }
}

// --------------------------- //
// Header breadcrumbs         //
// ------------------------- //

//initialize dropdowns
function hideDropDowns () {
  var dropdowns = document.getElementsByClassName('select');
  for (var i = 0; i < dropdowns.length; i++) {
    dropdown = dropdowns[i];
    Element.addClassName(dropdown, 'hide-dropdown');
  }
} 

//observe dropdown opener
function observeOpener(opener) {
  var dropdown = opener.parentNode;
  if (Element.hasClassName(dropdown, 'hide-dropdown')) {
    Element.removeClassName(dropdown, 'hide-dropdown');    
  } 
  else {
    Element.addClassName(dropdown, 'hide-dropdown');
  }
 return false;
}

//observing dropdown option
function observeOption(event) {
  var option = Event.element(event);
  var value = option.childNodes[0].nodeValue;
  var itemid = option.getAttribute('value');
  var selectbox = Event.findElement(event, 'ul');
  setDDValue(selectbox.parentNode, itemid, value);
  Element.addClassName(selectbox.parentNode, 'hide-dropdown');    
  Event.stop(event);
}

//observing dropdown options
function observeDropDownOptions(select) {
  var options = document.getElementsByTagName('li');
  for (var j = 0; j < options.length; j++) {
   var option = options[j];
   Event.observe(option, 'click', observeOption, false);
  }
}

function togglePairVisib(first, second, opt) {
  if(opt == true)  {
    first.style.display = "";
    second.style.display = "none";
  }
  else  {
    first.style.display = "none";
    second.style.display = "";
  }
}

//init ul
function initul(parent) {
  var list = "";
  var f = 0;
  var fc = "";
  for(i in parent) {
    if(parent[i].name) {
      if(f == 0) {fc = i; f++;}
      list += '<li value="' + i + '">' + parent[i].name + '</li>';
    }
  }
  return {"fc": fc, "list": list};
}

//init activities
function initacts(parent) {
    var temp = initul(parent);
    var acts = temp.list;
    var fc = temp.fc;
    $('sel_act').innerHTML = "";
    $('sel_act_i').value = fc;
    if(acts) {
      togglePairVisib($('sel_act_a'), $('sel_act_span'), true);
      $('sel_act_a').innerHTML = parent[fc].name;
      $('sel_act_span').innerHTML = "";
      new Insertion.Bottom('sel_act', acts);
      observeDropDownOptions($('sel_act'));
      //if selected breadcrumbs != current breadcrumbs display save link
      if(curr_axis != $('sel_axis_a').innerHTML || curr_yard != $('sel_yard_a').innerHTML || curr_act != $('sel_act_a').innerHTML) 
        togglePairVisib($('sel_save'), $('sel_back'), true);
      else
        togglePairVisib($('sel_save'), $('sel_back'), false);
    }
    else {
     togglePairVisib($('sel_act_a'), $('sel_act_span'), false);
     $('sel_act_span').innerHTML = noactivities;
     togglePairVisib($('sel_save'), $('sel_back'), false);
    }
}

//inityards
function inityards(parent) {
  var temp = initul(parent);
  var fc = temp.fc;
  var yards = temp.list;
  $('sel_yard').innerHTML = "";
  $('sel_yard_i').value = fc;
  if(yards) {
    togglePairVisib($('sel_yard_a'), $('sel_yard_span'), true);
    $('sel_yard_a').innerHTML = parent[fc].name;
    $('sel_yard_span').innerHTML = "";
    new Insertion.Bottom('sel_yard', yards);
    observeDropDownOptions($('sel_yard'));
  }
  else {
     togglePairVisib($('sel_yard_a'), $('sel_yard_span'), false);
     $('sel_yard_span').innerHTML = noyards;
  }
} 

//setting dropdown value into hidden field
function setDDValue(dropdown, itemid, value) {
  var a = dropdown.getElementsByTagName('a')[0];
  var input = dropdown.getElementsByTagName('input')[0];
  Element.update(a, value);
  //update the input for the current breadcrumb
  if (input) {
    input.value = itemid;
  }
  //selecting an axis will load yard and activity options for it
  if(a.id.indexOf('axis') > 0)  {
   var c = root[itemid].childs; // yards
   inityards(c);
   //init activity select according to the first selected yard
   var ys = $('sel_yard_i').value;
   if(ys) {
    var c = root[itemid].childs[ys].childs; //activities
    initacts(c);
   }
   // there no yards, so no activities
   else {
     togglePairVisib($('sel_act_a'), $('sel_act_span'), false);
     $('sel_act_span').innerHTML = noactivities;
     togglePairVisib($('sel_save'), $('sel_back'), false);
   }
  }
  //selecting a yard will load activities options for it
  else if(a.id.indexOf('yard') > 0)  {
      var c = root[$('sel_axis_i').value].childs[itemid].childs;
      initacts(c);
  }
  else if(a.id.indexOf('act') > 0) {
     //if selected breadcrumbs != current breadcrumbs display save link
      if(curr_act != $('sel_act_a').innerHTML) 
        togglePairVisib($('sel_save'), $('sel_back'), true);
      else
        togglePairVisib($('sel_save'), $('sel_back'), false);
  }
}

//closing dropdown by ESC
function onKeyPress(event) {
  switch(event.keyCode) {
    case Event.KEY_ESC: hideDropDowns(event);
    Event.stop(event);
    return;
  }
}

//closing dropdown by click out of it
function hideDropDownsOnClick(event) {
  var a = Event.element(event);
  if (a.tagName == 'a') { return false; }
  var dropdowns = document.getElementsByClassName('select');
  for (var i = 0; i < dropdowns.length; i++) {
    dropdown = dropdowns[i];
     Element.addClassName(dropdown, 'hide-dropdown');
  }
}

function saveChanges() {
   var name = $('sel_act_a').innerHTML;
   var ax = $('sel_axis_i').value;
   var y = $('sel_yard_i').value;
   var ac = $('sel_act_i').value;
   var page = root[ax].childs[y].childs[ac].obj;
   var t = true;
   if(page == "OrphanActivity")
    t = false;
   projectcontainerChange(name, page, t);    
}

function projectcontainerChange(name, page, displayConfirm) {
  if (displayConfirm) {
    if(confirm(confirmprojectcontainerchange)) {
      $('ChronoClasses.ProjectClass_0_container').value = page;
      var vis = $('ChronoClasses.ProjectClass_0_container_visible');
      vis.value = name;
      projectdata_save();
    }
  }
  else {
     $('ChronoClasses.ProjectClass_0_container').value = page;
     var vis = $('ChronoClasses.ProjectClass_0_container_visible');
     vis.value = name;
     projectdata_save();
  } 
}


