/**
 * Some general routines which might be useful in different parts of the webapp.
 */

/*
 * Global namespace used with content handling mechanism
 */
var ContentHandling = {
 		// namespace for test results JS callbacks
		testResults : {},
		// namespace for test scripts
		tests : {}
};

/*
 * JS Namespace used as base for global operations and variables of entire
 * application
 */
var DLApp = {
	
	DM_SERVLET : function(){ return "dmuseion"; },
	
	DL_SERVLET : function(){ return "dlibra"; },
	
	/* ajax call for component template */
	componentAjaxCall : function(componentName,pageId,requestCallback,requestParameters){
		var querystring = 'wid=ComponentRenderer&onlydata=false&name='+componentName+'&pageId='
		+ pageId + (typeof requestParameters != 'undefined' ? '&' + requestParameters : '');
		jQuery.ajax({
			   type: "POST",
			   url : homePageServletUrl+'/ajax.xml',
	   		   dataType : "xml",
			   data: querystring,
			   success : function(data){
					var body = data.getElementsByTagName("ComponentRenderer")[0].childNodes[0].nodeValue;
					requestCallback(body);
			   }
		});
	}
		
};

/**
 * JS Object script used mainly in page layouts.
 */

var PageUtils = {
	
	pageId : "",
	
	init : function(){
		var that = this;
		$(".leftDiv").each(function(){
			var pos = $(this).attr('id');
			var display;
			var conDiv = $(this).next();
			pos = pos.replace("ldi_","");
			display = getCookie("ldi_"+ that.pageId + "_" + pos);
			if(display == "hidden"){
				conDiv.hide();
				$(this).children(".sh_button").attr('src', homePageUrl + 'style/dlibra/' + userStyleVariant + '/l_close.gif');
			}else{
				conDiv.show();
				$(this).children(".sh_button").attr('src', homePageUrl + 'style/dlibra/' + userStyleVariant + '/l_open.gif');
			}
		});
		this.initAccountMenu();
	},
	
	/*
	 * Expands right side of the layout if left is empty. 
	 * Compatible only with simple-inv.vm layout
	 */
	autoExpand: function(){
		if(!($(".left").children().length)){
			$(".left").hide();
			$(".right").css({'width':'100%'});
		}
	},
	
	menuHide : false,
	
	initAccountMenu : function(){
		
		var that = this;
		
		// prepare popup event handlers
		$("#accountLink, #account-menu").bind("mouseover",function(){
			that.menuHide = false;
			var realPos = PageUtils.findRealPosition($("#accountLink")[0]);
			$("#account-menu").css({
				'position':'absolute',
				'top':realPos[1] + 25,
				'left':realPos[0] - 130
			}).show();
		});
		
		$("#accountLink, #account-menu").bind("mouseout",function(){
			that.menuHide = true;
			setTimeout("PageUtils.hideAccountMenu()",2000);
		});
		
		// call menu component through ajax
		var callback = function(body){
			$("#account-menu-loading").remove();
			$("#account-menu").prepend(body);
		};
		DLApp.componentAjaxCall("pl.psnc.dlibra.web.comp.pages.components.AccountMenuComponent",pageId,callback);
	},
	
	hideAccountMenu : function(){
		if(this.menuHide){
			$("#account-menu").hide();
		}
	},
	
	documentWidth : function(){

		return window.innerWidth != null ? window.innerWidth
				: document.documentElement
						&& document.documentElement.clientWidth ? document.documentElement.clientWidth
						: document.body != null ? document.body.clientWidth
								: null;

	},

	documentHeight : function() {

		return window.innerHeight != null ? window.innerHeight
				: document.documentElement
						&& document.documentElement.clientHeight ? document.documentElement.clientHeight
						: document.body != null ? document.body.clientHeight
								: null;

	},
	
	bindAccordionMechanism : function(){
		var that = this;
		$(".leftDiv").bind("click",function(){
			var conDiv = $(this).next();
			var pos;
			if(conDiv.is(":hidden")){
				conDiv.slideDown("fast");
				$(this).children(".sh_button").attr('src', homePageUrl + 'style/dlibra/' + userStyleVariant + '/l_open.gif');
				pos = $(this).attr('id');
				pos = pos.replace("ldi_","");
				// position expires - one week
				setCookie("ldi_" + that.pageId + "_" + pos, "visible", 604800000);
			}else{
				conDiv.slideUp("fast");
				$(this).children(".sh_button").attr('src', homePageUrl + 'style/dlibra/' + userStyleVariant + '/l_close.gif');
				pos = $(this).attr('id');
				pos = pos.replace("ldi_","");
				// position expires - one week
				setCookie("ldi_" + that.pageId + "_" + pos, "hidden", 604800000);
			}
		});
	},
	
	bindSlideMechanism : function(triggerSelector, targetSelector){
		$(triggerSelector).bind("click",function(){
			var target = $(targetSelector);
			if(target.is(":hidden")){
				target.slideDown();
			}else{
				target.slideUp();
			}
		});
	},
	
	/* finds real position of an element on the page */
	findRealPosition : function(obj){
		var curLeft = curTop = 0;
		if(obj.offsetParent){
			do{
				curLeft += obj.offsetLeft;
				curTop += obj.offsetTop;
			} while(obj = obj.offsetParent);
			return [curLeft,curTop];
		}
	},
	
	shorterString : function(original, maxLength){
		if(original.length > maxLength){
			original = original.substring(0,maxLength);
			return original + "...";
		}else{
			return original;
		}
	}
	
};

/**
 * Shows tooltip with menu definied within hidden
 * content container.
 * 
 * @param executor
 * 		ID of executor
 * @param content
 * 		ID of content container
 * @returns
 */
function MenuTooltip(executor,content, offset){
	
	this.executor = executor;
	this.content = content;
	this.skipHide = false;
	this.offset = typeof offset != 'undefined' ? offset : [0,0];
	
	this.hideTooltip = function(){
		if(this.skipHide == false){
			$("#" + this.content).fadeOut('fast');
		}
	};
	
	var that = this;
	$("#"+ executor + ", #" + content).bind("mouseover",function(){
		that.skipHide = true;
		var pos = PageUtils.findRealPosition(document.getElementById(that.executor));
		$("#"+that.content).css({position:'absolute',display:'block',top:pos[1] + that.offset[1],left:pos[0] + that.offset[0]}).show('slow');
	});
	
	$("#"+ executor + ", #" + content).bind("mouseout",function(){
		that.skipHide = false;
		setTimeout(that.hideTooltip.bind(that),1000);
	});
	
}

/**
 * CollectionsComponent JS Object
 */
var CollectionsComponent = {
  
  pageId : '',
  
  ajaxWarning : '',
  
  rootCollectionId : '',
  
  selectedDirs : '',
  
  urlSuffix : '',
  
  initialized : false,
  
  init : function(){
  	// $(".container_4_add").css({'margin-top':'30px'});
	this.initialized = true;
  },
  
  selectDir : function(dirid, urlS, forceSelect){
	if(typeof urlS != 'undefined'){
		this.urlSuffix = urlS;
	}
  	// skip reload is false by default
  	if(typeof forceSelect == 'undefined'){
  		forceSelect = false;
  	}
  	
    var span = $("#colNode_"+dirid);
	var li = $("#colList_"+dirid);
	
	var selStyle = span.attr('class');
	if(selStyle == "colSelected" && forceSelect == false){
		var rg = new RegExp("(^"+dirid+"_)" + "|" 
				+ "(_"+dirid+"_)" + "|" 
				+ "(_"+dirid+"$)" + "|" 
				+ "(^"+dirid+"$)","g");
		this.selectedDirs = this.selectedDirs.replace(rg,"_");
		this.selectedDirs = this.selectedDirs.replace("__","_");
		span.attr('class','colUnselected');
		span.attr('style','font-weight:normal;');
		$("#col_chkbox_on_"+dirid).hide();
		$("#col_chkbox_off_"+dirid).show();
	}
	else{
		span.attr('class','colSelected');
		span.attr('style','font-weight:bold;');
		$("#col_chkbox_on_"+dirid).show();
		$("#col_chkbox_off_"+dirid).hide();
		if(this.selectedDirs.indexOf(dirid) > -1 ){
			return;
		}
		if(this.selectedDirs == ""){
			this.selectedDirs = dirid;
		}else{
			this.selectedDirs = this.selectedDirs + "_" + dirid;
		}
		this.selectedDirs = this.selectedDirs.replace("__","_");
		
		// deselect main collection
		var rgroot = new RegExp("(^"+this.rootCollectionId+"_)" + "|" 
						+ "(_"+this.rootCollectionId+"_)" + "|" 
						+ "(_"+this.rootCollectionId+"$)" + "|" 
						+ "(^"+this.rootCollectionId+"$)","g");
		this.selectedDirs = this.selectedDirs.replace(rgroot,"_");
		this.selectedDirs = this.selectedDirs.replace("__","_");
		if(this.pageId == "aresults"){
			this.deselectParents(dirid);
		}
	}
	// do not reload
	if(forceSelect){
		return;
	}
	if(this.pageId == "results"){
		this.doChangeDir('normal');
	}else if(this.pageId == "aresults"){
		this.doChangeDir('advance');
	}
	
  },
  
  deselectParents : function(dirid){
  		var that = this;
		$("#colList_"+dirid).parents(".expandedColl").each(function(){
			var id = $(this).attr('id');
			that.deselectNode(id);	
		});
		$("#colList_"+dirid).find("li").each(function(){
			var id = $(this).attr('id');
			that.deselectNode(id);
		});
  },
  
  deselectNode : function(id){
  	 	id = id.replace("colList_","");
		$("#col_chkbox_on_"+id).hide();
		$("#col_chkbox_off_"+id).show();
		$("#colNode_"+id).attr('class','colUnselected');
		var rg = new RegExp("(^"+id+"_)" + "|" 
				+ "(_"+id+"_)" + "|" 
				+ "(_"+id+"$)" + "|" 
				+ "(^"+id+"$)","g");
		this.selectedDirs = this.selectedDirs.replace(rg,"_");
		this.selectedDirs.replace("__","_");
  },
  
  showWaitWarn : function(){
  		var warning =  getAjaxWarning(this.ajaxWarning, homePageUrl);
		jQuery.blockUI(warning);
  },
  
  deselectAll : function(){
  	if(this.selectedDirs == ""){
		return;
	}
  	$(".collImg").each(function(){
		if($(this).attr('id').indexOf("col_chkbox_on") > -1 ){
			$(this).hide();
		}else if($(this).attr('id').indexOf("col_chkbox_off") > -1){
			$(this).show();
		}
	});
	$(".colSelected").attr('class','colUnselected');
	this.selectedDirs = "";
	if(this.pageId == "results"){
		this.doChangeDir('normal');
	}else if(this.pageId == "aresults"){
		this.doChangeDir('advance');
	}
  },
  
  doChangeDir : function(type){
  	 setTimeout("CollectionsComponent.showWaitWarn()",500);
  	 $("#dirids").val("");
  	 if( typeof SearchAmbiguousAttributesComponent != 'undefined' ){
       	if(type == "advance"){
			SearchAmbiguousAttributesComponent.prepareAdvForm();
		}else{
			SearchAmbiguousAttributesComponent.prepareForm(true);
		}
	 } else {
       if(type == "advance"){
    	   var url = homePageServletUrl + "/" + this.pageId + "?action=SearchAction&mdirids=" + this.selectedDirs + "&skipSearch=false" + this.urlSuffix + "&rootid=" + SearchResultsComponent.rootid;
    	   window.location.href = url;
       }else{
    	   prepareForm();
       }
	 }
  }
	  
};


function quickLogin(){
	var iframe = parent.document.getElementById("nyroModalIframe");
	var destUrl;
	if(DLAppVars.loginLink != ''){
		destUrl = DLAppVars.loginLink;
	}else{
		destUrl = homePageServletUrl+'/login-select';
	}
	if(typeof iframe != 'undefined' && iframe != null){
		iframe.src = destUrl;
	}else{
		if(DLAppVars.loginLink != ''){
			parent.window.location.href = DLAppVars.loginLink;
		}else{
			parent.window.location.href = homePageServletUrl+'/login-select';
		}
	}
}

function quickRegister(){
	parent.window.location.href = homePageServletUrl+'/new-account';
}

function showLoginInfo(){
	 $("#securedContent").hide();
	 $("#loginReminder").fadeIn('slow');
}

/* global collection id variable */
var DIR_ID = "";

function collectionTree(){
	window.location.href = homePageServletUrl + "/collections?dirids="+DIR_ID;
}

/**
 * Mechanism for splitting long sentences in edition/publication attribute
 * values view.
 */
function splitLongSentences(){
	// select old view
	var text = $("span.attribute > a");
	 for(var i = 0 ; i < text.length ; i++){
	  if(text[i].id == ""){
	  	 continue;
	  }
	  var anchor = $("#"+text[i].id);
	  var funcName = (text[i].id).replace("attr_anch_","");
	  var results = anchor.text();
	  results = results.match(/.*?(\.|,|\!|\?|\(|\)|$)/g);
	  var splitted = "";
	  if(results != null){
	   // create new view - with multiple anchors
	   for(var j = 0; j < results.length; j++){
	     splitted += ("<a class='splittedAttrValue' id='pmcA_"+funcName+"_"+j+"' href='#'>"+results[j]+"</a>");
	   }
	   anchor.replaceWith(splitted);
	   for(j = 0 ; j < results.length ; j++){
	    // assign new action to each anchor
		 $("#pmcA_"+funcName+"_"+j).live("click",function(){
	      var txt = $(this).text();
	  	    var idInfo = $(this).attr('id').replace("pmcA_","");
	  	    idInfo = (idInfo.match(/.*?(\_)/g)[0]).replace("_","");
	  	    submitQuery(idInfo, txt.replace(/(\.|,|\!|\?|\(|\)|)/g,""));  
	  	   });
	  	  }
	   }
	 }
}

function fitMiniatures(){
	var arrImg = $(".minImg");
	_fitMiniature(0,arrImg);
}

function _fitMiniatures(containerClass,imgClass, skipLeftMargin){
    var arrImg = $("."+imgClass);
	__fitMiniature(0,arrImg,containerClass, skipLeftMargin);
}

function __fitMiniature(index, arrImg, containerClass, skipLeftMargin){
	if(index >= arrImg.length) return;
	var img = arrImg[index];
	var cW = parseInt($("."+containerClass).css('width'));
	var cH = parseInt($("."+containerClass).css('height'));
	if(img.complete){
		var perc = calculateFit(cW,cH,img.width,img.height);
		if(perc != Infinity){
			resizeAndFit(perc,img,cW,cH,skipLeftMargin);
		}
		__fitMiniature(index+1,arrImg, containerClass, skipLeftMargin);
	}
	else{
		$(img).load(function(){
			var perc = calculateFit(cW,cH,img.width,img.height);
			if(perc != Infinity){
				resizeAndFit(perc,img,cW,cH,skipLeftMargin);
			}
			__fitMiniature(index+1,arrImg, containerClass, skipLeftMargin);
		});
	}
}

function _fitMiniature(index,arrImg){
	__fitMiniature(index, arrImg, "minContainer");
}

function fitMiniature(id,url){
	$("#"+id).replaceWith("<img id=\""+id+"\" src=\""+url+"\" class=\"minImg\"/>");
	var img = $("#"+id)[0];
	var cW = parseInt($(".minContainer").css('width'));
	var cH = parseInt($(".minContainer").css('height'));
	if(img.complete){
		var perc = calculateFit(cW,cH,img.width,img.height);
		resizeAndFit(perc,img,cW,cH);
	}
	else{
		$(img).load(function(){
			var perc = calculateFit(cW,cH,img.width,img.height);
			resizeAndFit(perc,img,cW,cH);
		});
	}
}

/**
 * Resizes loaded miniature image and fits it to container width/height.
 * 
 * @param percentage
 *            level of resize
 * @param img -
 *            image to resize
 * @param conWidth -
 *            container width
 * @param conHeight -
 *            container height
 * 
 */
function resizeAndFit(percentage, img, conWidth, conHeight, skipLeftMargin){
	var pWidth = parseInt(img.width*(percentage/100));
	var pHeight = parseInt(img.height*(percentage/100));
	var marginLeft = 0;
	var marginTop = 0;
	if(!skipLeftMargin){
		marginLeft = parseInt((conWidth - pWidth)/2);
	}
 	marginTop = parseInt((conHeight - pHeight)/2);
    $(img).css({
		'width' : pWidth,
		'height' : pHeight,
		'margin-left' : marginLeft,
		'margin-top' : marginTop
	});
}

/*
 * Calculating percents of fit option : how many percents are needed to fit an
 * image into a container. initX,initY : container size parameters currentWidth,
 * currentHeight : to fit object size
 */
function calculateFit(initX, initY, currentWidth, currentHeight){
	var l_cont;
	var l_img;
	var perc;
	var perc2;
	l_img = currentWidth;
	l_cont = initX;
	perc = ((l_cont*100)/l_img);
	perc = parseFloat(perc.toFixed(1));
	l_img = currentHeight;
	l_cont = initY;
	perc2 = ((l_cont*100)/l_img);
	perc2 = parseFloat(perc2.toFixed(1));
	if(perc < perc2)
		return perc;
	else
		return perc2;
}

function getRequestParam(contentUrl, paramName) 
{
	var requestArg = paramName+"=";
	var paramIndex = contentUrl.indexOf(requestArg);
	if ( paramIndex == -1 ) return null;
	
	var nextAmp = contentUrl.indexOf("&", paramIndex);
	if ( nextAmp == -1 ) 
	{
		return contentUrl.substring(paramIndex+requestArg.length, contentUrl.length);
	}
	return contentUrl.substring(paramIndex+requestArg.length, nextAmp);
}

/**
 * Removes action parameter from page URL and send redirect to page without
 * action param.
 */
function safePageRefresh() 
{   
   var refreshingUrl = removeAction(window.location.href);
   var hashIndex = refreshingUrl.indexOf("#"); 
   if (  hashIndex != -1 ) {
	   refreshingUrl = refreshingUrl.substring(0,hashIndex);
   }

   var showContentParam = "showContent=true";
   var hasShowContent = refreshingUrl.indexOf(showContentParam);
   if ( hasShowContent != -1 ) {
	   refreshingUrl = refreshingUrl.replace(showContentParam, "showContent=false");
   }
   
   window.location.href = refreshingUrl;
}

/** Function is cutting too long names */
var NAME_MAX_LENGTH = 100;

function shorterNames(){
		$('.dlibra_shortNames a').each(function () {
				var pub_name = this.innerHTML;
				if(pub_name.length > NAME_MAX_LENGTH){
					pub_name = pub_name.substring(0,(NAME_MAX_LENGTH-3));
					this.innerHTML = pub_name + "...";
				}
		
		});
}

function _shorterNames(selector, maxLength){
	$(selector).each(function(){
		var name = $(this).text();
		name = $.trim(name);
		if(name.length > maxLength){
			name = name.substring(0,maxLength);
			$(this).text(name + "...");
			if(this.id == ""){
				this.id = "dL_shorter";
			}
		}
	});
}

function __shorterNames(object, maxLength){
	var name = $(object).text();
	if(name.length > maxLength){
		name = name.substring(0,maxLength);
		$(this).text(name + "...");
		if(this.id == ""){
			this.id = "dL_shorter";
		}
	}
}

function _cutString(value){
	return cutString(value,500);
}

function cutString(value, maxLenght){
	if(value.length > maxLenght){
		value = value.substring(0,(maxLenght-3));
		value = value + "...";
	}
	return value;
}

function isInteger (s)
{
    var i;

     if (isEmpty(s))
     if (isInteger.arguments.length == 1) return 0;
     else return (isInteger.arguments[1] == true);

     for (i = 0; i < s.length; i++)
     {
        var c = s.charAt(i);

        if (!isDigit(c) ){
	         return c == "-";
        }
     }

    return true;
}

/** You must specify value of DOM element */
function isEmpty(s)
{
   return ((s == null) || (s.length == 0))
}

function isDigit (c)
{
     return ((c >= "0") && (c <= "9"))
}

/** dom element as argument */
function isVisible( c ) {
  return c.style.display != 'none';
}

/**
 * checks whether given email address is valid.
 */
function isEmailValid(email) {
   var reg = /^([A-Za-z0-9_\-\.])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,4})$/;
   return reg.test(email);
}


function BrowserCheck() {
	var b = navigator.appName
	if (b=="Netscape") this.b = "ns"
	else if (b=="Microsoft Internet Explorer") this.b = "ie"
	else this.b = b
	this.version = navigator.appVersion
	this.v = parseInt(this.version)
	this.ns = (this.b=="ns" && this.v>=4)
	this.ns4 = (this.b=="ns" && this.v==4)
	this.ns5 = (this.b=="ns" && this.v==5)
	this.ie = (this.b=="ie" && this.v>=4)
	this.ie4 = (this.version.indexOf('MSIE 4')>0)
	this.ie5 = (this.version.indexOf('MSIE 5')>0)
	this.ie6 = (this.version.indexOf('MSIE 6')>0)
	this.ie7 = (this.version.indexOf('MSIE 7')>0)
	this.ff  = (navigator.userAgent.indexOf("Firefox")>0)
	this.ff2 = (navigator.userAgent.indexOf("Firefox/2")>0)
	this.min = (this.ns||this.ie)
}
is = new BrowserCheck()

var stat

function statBar(txt,time) {
   clearTimeout(stat);
   window.status = txt;
   stat=setTimeout("statErase()",time);
}

function statErase() {
   window.status="dLibra - biblioteka cyfrowa";
}

function preload(imgObj,imgSrc) {
	if (document.images) {
		eval(imgObj+' = new Image()')
		eval(imgObj+'.src = "'+imgSrc+'"')
	}
}

function changeImage(layer,imgName,imgObj) {
	if (document.images) {
		if (document.layers && layer!=null) eval('document.'+layer+'.document.images["'+imgName+'"].src = '+imgObj+'.src')
		else document.images[imgName].src = eval(imgObj+".src")
	}
}

function setCookie(name, value, expires, path, domain)
{
    var exp = new Date();
    var ex = new Date(exp.getTime()+expires);
    var curCookie = name + "=" + escape(value) +
      ((expires) ? "; expires=" + ex.toGMTString() : "") +
      ((path) ? "; path=" + path : "") +
      ((domain) ? "; domain=" + domain : "");
    document.cookie = curCookie; 
} 

function getCookie ( cookie_name )
{
  var results = document.cookie.match ( cookie_name + '=(.*?)(;|$)' );

  if ( results )
    return ( unescape ( results[1] ) );
  else
    return null;
}

function hideLinks( whichSelect, link, val )
{
   if(document.forms.collections[whichSelect].value == val)
   {
     document.getElementById(link).style.visibility = 'hidden';
   }
   else
   {
     document.getElementById(link).style.visibility = 'visible';
   }
}

/*
 * hide all select boxes on page.
 */
function hideSelect(){
	$("select").hide();
}

/*
 * shows all select boxes on page
 */
function unhideSelect(){
	$("select").show();
}


function showPopup(homepageUrl, subPage, windowName) {
  pictureWindow = window.open(homepageUrl + subPage,windowName,'scrollbars=yes,toolbar=no,location=no,directories=no,status=no,menubar=no,resizable=yes,height=550,width=470');
  pictureWindow.focus();
}

function showHelp(helpId, homepageUrl) {
  showPopup(homepageUrl, mainServletName+"/help?id=" + helpId,"dlibra_help_window");
}

function showContact(homepageUrl) {
	showPopup(homepageUrl, mainServletName+"/contact","dlibra_contact_window");
}

function showElementContact(id, homepageUrl) {
	  showPopup(homepageUrl, mainServletName+"/elementcontact?id="+id,"dlibra_element_contact_window");
}


/**
 * prepares box with ajax please wait dialog
 * 
 * @param message -
 *            message which will be displayed.
 * @param baseUrl -
 *            base url of webpage, you must specify this when using this method
 *            from main page.
 */
function getAjaxWarning(message, baseUrl, boolek) 
{
	 if (  baseUrl == null ){
		return  message + '<br/> <img src="../style/common/img/pleasewait.gif" alt="[||--]" />';
	 }
 	 else{
 		 baseUrl = baseUrl + '/style/common/img/pleasewait.gif';
 		 baseUrl = baseUrl.replace("//style","/style");
 		 return  message + '<br/> <img src="'+ baseUrl +'" alt="[||--]" />';
 	 }
		
}


/**
 * puts message into dom, and hides it after some time
 * 
 * @param message -
 *            message which will be displayed.
 * @param isSuccess -
 *            if true success image will be used
 * @param messageDomId -
 *            dom id of message object which will be created.
 * @param statusDomId -
 *            dom id of object which will be appended with messageDom object.
 * @param timeout -
 *            optional time, default value is 2000;
 * @param cssClass -
 *            optional css class name which will be assigned to message.
 * @param baseUrl -
 *            base url of webpage, use this always when invoking this method on
 *            main webpage.
 */
function statusMessage(message, isSuccess, messageDomId, statusDomId, timeout, cssClass, baseUrl)
{
  if ( cssClass == null )   {
	  cssClass = 'error';
	  if ( isSuccess ) cssClass = 'success';
  }  
  if ( timeout == null ) 
      timeout=2000;
      
  if  ( baseUrl == null )
	  baseUrl = '../'; 
      
  var imageUrl = baseUrl+'/style/common/img/icons/statusok2.gif';
  if ( !isSuccess ) {
      imageUrl = baseUrl+'/style/common/img/icons/statuserror2.gif';
  }    
  $('#'+statusDomId).html('<span id="'+messageDomId+'" class="'+cssClass+'"><img src="'+imageUrl+'" alt="&nbsp" style="vertical-align:bottom"/>&nbsp;'+message+'</span>');
  setTimeout('$("#'+messageDomId+'").hide("medium")', timeout);
}

/**
 * This function removes action parameter and its value from given url. Use this
 * function always when you want to send dlibra url to external system.
 * 
 * @param url -
 *            page url
 */
function removeAction(pageUrl) {
    var url = new String(pageUrl);
	var result = url;
	var startIndex = url.indexOf("?action=");
	if (startIndex == -1) {
		startIndex = url.indexOf("&action=");
	}
	if (startIndex != -1) {
		var secondPart = url.substring(startIndex + 1);
		var endPart = secondPart.indexOf('&');
		if (endPart == -1) {
			result = url.substring(0, startIndex);
		}
		else {
			result = url.substring(0, startIndex + 1)
					+ secondPart.substring(endPart + 1);
		}
	}
	return result;
}

function replaceAll( str, searchTerm, replaceWith, ignoreCase )	{
	var regex = "/"+searchTerm+"/g";
	if( ignoreCase ) regex += "i";
	return str.replace( eval(regex), replaceWith );
}
