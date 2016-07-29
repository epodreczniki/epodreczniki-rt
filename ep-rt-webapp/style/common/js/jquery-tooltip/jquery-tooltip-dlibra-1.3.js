/**
 * DLibra tooltip support operations.
 */


/*
 * Class maps dLibra edition informations to javascript objects.
 * 
 * @param id - id of edition @param hasImage - has it miniature @param
 * attributes - attributes references
 * 
 */
function EditionInfo(id, hasImage, attributes, type){
  	this.id = id;
  	this.hasImage = hasImage;
  	this.attributes = attributes;
  	this.type = type;
} 
  
/*
 * Class maps dLibra attributes to javascript objects.  
 * 
 * @param name - localized name of attribute
 * @param values - localized values of attribute
 * @param isTitle - is this a title
 * 
 */
function Attribute(name, values, isTitle){
   	this.name = name;
   	this.values = values;
   	this.isTitle = isTitle
}

/*
 * Calls Ajax request for additional editions informations.
 * Retrieved informations are passed to 'callback' function.
 * Editions array contains object of 'EditionInfo' type. 
 * 'editionIds' should be a String value in form : '[id 1]_[id 2]_[...]_[id n]', 
 * where [id n] is long value of id.
 *
 * @param editionIds
 * @param pageId
 * @param callback
 * 
 */
function getEditionInformations(editionIds, pageId, callback){
	var querystring = 'wid=ComponentRenderer&onlydata=true&name=pl.psnc.dlibra.web.comp.pages.components.ExtendEditionInfoComponent&pageId='+pageId+'&ids='+editionIds;
  	jQuery.ajax({
		   type: "POST",
		   url : homePageServletUrl+'/ajax.xml',
   		   dataType : "xml",
		   data: querystring,
		   success : function(data){
		   		callback(tooltipContentFromXML(data, true));
		   }
	});
}

function tooltipContentFromXML(data, ajax){
	
	if(ajax){
		var body = data.getElementsByTagName("ComponentRenderer")[0].childNodes[0].nodeValue;
	}
	else{
		var body = data;
	}
	
		try // Internet Explorer
		{
			xmlDoc=new ActiveXObject("Microsoft.XMLDOM");
			xmlDoc.async="false";
			xmlDoc.loadXML(body);
			body = xmlDoc;
		}
		catch(e)
		{
			parser=new DOMParser();
			xmlDoc=parser.parseFromString(body,"text/xml");
			body = xmlDoc;
		}
		 var elements = body.getElementsByTagName("element");
		 editionInfos = new Array(elements.length);
		 for(var i = 0 ; i < elements.length; i++){
			 var el = elements[i];
			 var id = el.getAttribute("id");
			 var hasImage = el.getAttribute("hasImage");
			 var type = el.getAttribute("type");
			 var attrs = el.getElementsByTagName("attribute");
			 var attributes = new Array(attrs.length);
			 var editionInfo;
			 for(var j = 0 ; j < attrs.length; j++){
				 var pos = parseInt(attrs[j].getAttribute("position"));
				 var isTitle = attrs[j].getAttribute("isTitle");
				 var name = attrs[j].getElementsByTagName("name")[0].childNodes[0].nodeValue;
				 var values = attrs[j].getElementsByTagName("values")[0].childNodes[0].nodeValue;
				 attributes[pos] = new Attribute(name,values,isTitle);
			 }
			 editionInfo = new EditionInfo(id,hasImage,attributes, type);
			 editionInfos[i] = editionInfo;  		 	 
		 }
	return editionInfos;
}
 
 
/*
 * Generates toolitps for group of edition anchors.
 * Container of anchors is retrieved by CSS selector.
 * Also to connect selected anchors with concrete edition every anchor should contain
 * id of edition in its "id" tag attribute. Id of edition is retrieved by removing 'id_prefix' from
 * "id" attribute value. For general purpose it is necessary to transmit name of component which
 * uses this function. 
 * 
 * @param editionInfos
 * @param selector - anchor container CSS selector
 * @param id_prefix
 * @param titleLabel
 * @param bodyHandler
 */
function generateTooltips(editionInfos, selector, id_prefix, titleLabel, bodyHandler){
     $(selector).tooltip({
			top: 5,
			left : 5,
			track: false,
			delay: 0,
			showURL: false,
			bodyHandler: function(){
  				var id = $(this).attr('id');
  				id = id.replace(id_prefix,"");
  				var prefix;
  				if(id.charAt(0) == 'p'){
  					prefix = 'p';
					id = id.substring(1, id.length);
  				}else if(id.charAt(0) == 'e'){
					prefix = 'e';
					id = id.substring(1, id.length);
				}else{
					prefix = 'e';
				}
  				var editionInfo;	
  				for(var i = 0 ; i < editionInfos.length; i++){
  						if(editionInfos[i].id == id){
  							if(prefix == 'p' && editionInfos[i].type == "publication"){
  								editionInfo = editionInfos[i];
  								break;
  							}else if(prefix == 'e' && editionInfos[i].type == "edition"){
  								editionInfo = editionInfos[i];
  								break;
  							}
  						}
  				}
  				if(bodyHandler == 'basic')
					return tooltipBasicBodyHandler(editionInfo,$(this).attr('name'),titleLabel);
				else
					return tooltipBodyHandler(editionInfo,id_prefix,$(this).attr('name'),titleLabel);
			},
			fade: jQuery.browser.msie ? 0 : 250
		});
	
}

/*
 *	Support function for tooltip body handler - 
 *	keeps references to once loaded images. 
 */
var imageMap = new Array();

/* TooltipImage class */
function TooltipImage(src,id,image){
	this.src = src;
	this.id = id;
	this.image = image;
}

function addImageToBuffer(element){
	for(var i = 0 ; i < imageMap.length ; i++){
		var elem = imageMap[i];
		if(imageMap[i].id = element.id){
			return imageMap[i];
		}
	}
	var img = new Image();
	img.src = element.src;
	element.image = img;
	imageMap[imageMap.length] = element;
	return element;
}

 /*
  * Generates body for edition informations tooltip.  
  * Renders edition miniature (if it has one) and its
  * attributes.  
  * If there are no attributes in edition, tooltip will render
  * 'label' as title.
  * 
  * @param editionInfo - of type EditionInfo
  * @param componentName - name of the component which renders tooltip.
  * @param label - title label of edition 
  * @param titleLabel - resource localized label 
  *
  */
 function tooltipBodyHandler(editionInfo,componentPrefix,label,titleLabel,imgSrc){
 		
 		var pubId = editionInfo.id;
 		editionInfo = titleCheckout(editionInfo,label,titleLabel);
 		var htmlCode;
 		// var msie = jQuery.browser.msie;
 		var top = "";
 		var bottom = "";
 		var gfxExt = ".gif";
 		if(!isMSIE6()){
 			top = "margin-bottom:-4px;";
 			bottom = "margin-bottom:6px;";
 			gfxExt = ".png";
 		}
 		if(editionInfo.hasImage == "true"){
 			var cNParts = componentPrefix.split('.');
 			componentPrefix = cNParts[cNParts.length-1];
 			var componentId = componentPrefix + '_divWithImage' + pubId; 
 			if(imgSrc == null){
 				imgSrc = homePageUrl + 'image/edition/' + pubId
 			}
 			var tooltipImage = new TooltipImage(imgSrc, pubId, null);
 			tooltipImage = addImageToBuffer(tooltipImage);
 			var miniImg = tooltipImage.image;
 			if(miniImg.complete){
 				// html code when image is complete.
 				htmlCode = '' +  
 				'<div style="width:350px;overflow:hidden;padding-left:11px;padding:10px;background:#eee;box-shadow:3px 3px 0px #999;"><div id="' 
 				+ componentId + '" class="tooltipImageDiv"><img style="background-color:#fff;" src="' + homePageUrl + 'image/edition/' + pubId + '" id="tooltipImage"></img>'
 				+ '</div><div>';
 				for(var ac = 0; ac < editionInfo.attributes.length; ac++){
 					var cattr = editionInfo.attributes[ac];
 					if(cattr.values != '???' && cattr.values != '')
 						if(ac == 0)
 							htmlCode += '<p id="tooltipBodyFontStrong"><strong>' + cattr.name + ' : </strong>' + _cutString(cattr.values) + '</p>';
 						else
 							htmlCode += '<p id="tooltipBodyFont"><strong>' + cattr.name + ' : </strong>' + _cutString(cattr.values) + '</p>';
 				}
 				htmlCode += '</div></div>';
 				
 			}
 			else{
 				$(miniImg).load(function(){
 					// replace loading css with loaded image.
 					$(this).hide();
 					$('#'+componentId).removeClass('tooltipImageDivLoading').addClass('tooltipImageDiv')
 					.append(this);
 					$(this).fadeIn();
 				}).attr('id','tooltipImage');
 					// loading html code
 					htmlCode = '' + 
 					'<div style="width:350px;overflow:hidden;padding-left:11px;padding:10px;background:#eee;box-shadow:3px 3px 0px #999;" ><div id="' + 
 					componentId + '" class="tooltipImageDivLoading" style="background:url(\''+ homePageUrl 
 					+ '/style/dlibra/' + userStyleVariant + '/jquery-tooltip/tooltip_loading.gif\') no-repeat center center;">'
 					+ '</div><div>';
 					for(var ac = 0; ac < editionInfo.attributes.length; ac++){
 						var cattr = editionInfo.attributes[ac];
 						if(cattr.values != '???' && cattr.values != '')
 							if(ac == 0)
 								htmlCode += '<p id="tooltipBodyFontStrong"><strong>' + cattr.name + ' : </strong>' + _cutString(cattr.values) + '</p>';
 							else
 								htmlCode += '<p id="tooltipBodyFont"><strong>' + cattr.name + ' : </strong>' + _cutString(cattr.values) + '</p>';
 					}
 					htmlCode+='</div></div>';
 					
 			}
 		}
 		else {
 			//no image container
 			htmlCode = '<div id="tooltipNoImageDiv" style="background:#eee;box-shadow:3px 3px 0px #999;padding:10px;width:350px;overflow:hidden;">';
 				for(var ac = 0; ac < editionInfo.attributes.length; ac++){
 					var cattr = editionInfo.attributes[ac];
 					if(cattr.values != '???' && cattr.values != '')
 						// float:left;
 						if(ac == 0)
 							htmlCode += '<p id="tooltipBodyFontStrong" style="margin-left:10px;"><strong>' + cattr.name + ' : </strong>' + _cutString(cattr.values) + '</p>';
 						else
 							htmlCode += '<p id="tooltipBodyFont" style="margin-left:10px;"><strong>' + cattr.name + ' : </strong>' + _cutString(cattr.values) + '</p>';
 				}
 				htmlCode+='</div>';
 		}
 	return htmlCode;
 } 
 
/*
 * Checks if edition title is empty and fills it with default label value.
 * Default title is also set when there are no attributes in edition info.   
 */
 function titleCheckout(editionInfo, label, titleLabel){
	 
	 for(var i = 0 ; i < editionInfo.attributes.length; i++){
			var curr = editionInfo.attributes[i];
			if(curr.isTitle == "true"){
				if(curr.values == "???"){
					curr.values = label;
				}
			}
		}
	 if(editionInfo.attributes.length == 0){
			var titleAttribute = new Attribute(titleLabel, label, "true");
			editionInfo.attributes = new Array(1);
			editionInfo.attributes[0] = titleAttribute;
	}
	return editionInfo;
 }
 
 function isMSIE6(){
	if(jQuery.browser.msie && parseFloat(jQuery.browser.version) < 7.0){
		return true;
	}
	return false;
 }
 
/*
 * Tooltip with just label.
 */   
function tooltipParticleBodyHandler(label, titleLabel){
	  	var elementName = label;
	 	var bottom = "";
	 	var top = "";
	 	var gfxExt = ".gif";
	 	if(!isMSIE6()){
	 		top = "margin-bottom:-4px;";
	 		bottom = "margin-bottom:6px;";
	 		gfxExt = ".png";
	 	}
	 	var htmlCode = '<img src="' + homePageUrl + 'style/dlibra/' + userStyleVariant + '/jquery-tooltip/tooltip-background_top'+gfxExt+'" style="' + top 
	 	+ '"/><div id="tooltipNoImageDiv" style="background:url(\''
	 	+ homePageUrl + 'style/dlibra/'+userStyleVariant+'/jquery-tooltip/tooltip-background'+gfxExt+'\') repeat-y; width:350px;overflow:auto;">';
	 	htmlCode += '<p id="tooltipBodyFont" style="margin-left:17px;">'; 
	 	if(titleLabel != '')
	 		htmlCode += '<strong>' + titleLabel + ' : </strong>'; 
	 	htmlCode += label + '</p>';
	 	htmlCode += '</div>' + '<img src="' + homePageUrl + 'style/dlibra/' + userStyleVariant + '/jquery-tooltip/tooltip-background_bottom'+gfxExt+'" style="' + bottom + '" />';
	 	return htmlCode;
 }
 
 /*
  * Tooltip with just label and additional font styling parameter.
  */   
 function tooltipStyledParticleBodyHandler(label, titleLabel, fontStyle){
 	  	var elementName = label;
 	 	var bottom = "";
 	 	var top = "";
 	 	var gfxExt = ".gif";
 	 	if(!isMSIE6()){
 	 		top = "margin-bottom:-4px;";
 	 		bottom = "margin-bottom:6px;";
 	 		gfxExt = ".png";
 	 	}
 	 	var htmlCode = '<img src="' + homePageUrl + 'style/dlibra/' + userStyleVariant + '/jquery-tooltip/tooltip-background_top'+gfxExt+'" style="' + top 
 	 	+ '"/><div id="tooltipNoImageDiv" style="background:url(\''
 	 	+ homePageUrl + 'style/dlibra/'+userStyleVariant+'/jquery-tooltip/tooltip-background'+gfxExt+'\') repeat-y; width:350px;overflow:auto;">';
 	 	htmlCode += '<p id="tooltipBodyFont" style="margin-left:17px;'+fontStyle+'">'; 
 	 	if(titleLabel != '')
 	 		htmlCode += '<strong>' + titleLabel + ' : </strong>'; 
 	 	htmlCode += label + '</p>';
 	 	htmlCode += '</div>' + '<img src="' + homePageUrl + 'style/dlibra/' + userStyleVariant + '/jquery-tooltip/tooltip-background_bottom'+gfxExt+'" style="' + bottom + '" />';
 	 	return htmlCode;
  }
 
 
 /* Basic tooltip body function for viewing edition informations */
 function tooltipBasicBodyHandler(editionInfo,label,titleLabel){
	editionInfo = titleCheckout(editionInfo, label, titleLabel); 
	var elementName = label;
 	var bottom = "";
 	var top = "";
 	var gfxExt = ".gif";
 	if(!isMSIE6()){
 		top = "margin-bottom:-4px;";
 		bottom = "margin-bottom:6px;";
 		gfxExt = ".png";
 	}
 	var htmlCode = '<img src="' + homePageUrl + 'style/dlibra/' + userStyleVariant + '/jquery-tooltip/tooltip-background_top'+gfxExt+'" style="' + top 
 	+ '"/><div id="tooltipNoImageDiv" style="background:url(\''
 	+ homePageUrl + 'style/dlibra/'+userStyleVariant+'/jquery-tooltip/tooltip-background'+gfxExt+'\') repeat-y; width:350px;overflow:auto;">';
 	for(var i = 0; i < editionInfo.attributes.length ; i++){
 		if(editionInfo.attributes[i].values != '???')
 		htmlCode += '<p id="tooltipBodyFont" style="margin-left:10px;"><strong>'
 			+ editionInfo.attributes[i].name +' : </strong>' + _cutString(editionInfo.attributes[i].values) + '</p>';
 	}
 	htmlCode += '</div>' + '<img src="' + homePageUrl + 'style/dlibra/' + userStyleVariant + '/jquery-tooltip/tooltip-background_bottom'+gfxExt+'" style="' + bottom + '" />';
 	return htmlCode;
 }
 
 /**
  * Cuts search result titles and generates tooltip for them.
  */
 function searchResultTitleTooltip(length){
	 if(length != null)
		 _shorterNames("a.dLSearchResultTitle span[id=src_titleLink_shorterTitle] span[class=src_titleLink_title]",length);
	 else
		 _shorterNames("a.dLSearchResultTitle span[id=src_titleLink_shorterTitle] span[class=src_titleLink_title]",90);
	 $('a.dLSearchResultTitle span[id=dL_shorter], a.dLSearchResultTitle span[id=src_titleLink_creator]').tooltip({
		delay: 0,
		showURL: false,
		top : -10,
		left : 5,
		bodyHandler : function(){
					if($(this).attr('id') == "src_titleLink_creator"){
						    var parent = $(this).parent().get(0);
						    var tchild = $(parent).children("span[class=src_titleLink_title]");
						    if(tchild != null && $(tchild).attr('id') == "dL_shorter"){
						     var parent = $(this).parent().get(0);
					     parent = $(parent).parent().get(0);
					      var child = $(parent).children("span[id=src_titleLink_fullTitle]");
					      return tooltipStyledParticleBodyHandler(child.html(),'','font-size:10pt;font-weight:bold;');
						    }
						    else{
						    	return "<div></div>";
						    }
					   }
					   else{
					    var parent = $(this).parent().get(0);
					    parent = $(parent).parent().get(0);
					    var child = $(parent).children("span[id=src_titleLink_fullTitle]");
					    return tooltipStyledParticleBodyHandler(child.html(),'','font-size:10pt;font-weight:bold;');
					   }
				},
			fade: 250
		});
}
 
 