/*! matchMedia() polyfill - Test a CSS media type/query in JS. Authors & copyright (c) 2012: Scott Jehl, Paul Irish, Nicholas Zakas. Dual MIT/BSD license */
window.matchMedia=window.matchMedia||(function(e,f){var c,a=e.documentElement,b=a.firstElementChild||a.firstChild,d=e.createElement("body"),g=e.createElement("div");g.id="mq-test-1";g.style.cssText="position:absolute;top:-100em";d.appendChild(g);return function(h){g.innerHTML='&shy;<style media="'+h+'"> #mq-test-1 { width: 42px; }</style>';a.insertBefore(d,b);c=g.offsetWidth==42;a.removeChild(d);return{matches:c,media:h}}})(document);
﻿(function(a,w){function f(a){p[p.length]=a}function m(a){q.className=q.className.replace(RegExp("\\b"+a+"\\b"),"")}function k(a,d){for(var b=0,c=a.length;b<c;b++)d.call(a,a[b],b)}function s(){q.className=q.className.replace(/ (w-|eq-|gt-|gte-|lt-|lte-|portrait|no-portrait|landscape|no-landscape)\d+/g,"");var b=a.innerWidth||q.clientWidth,d=a.outerWidth||a.screen.width;h.screen.innerWidth=b;h.screen.outerWidth=d;f("w-"+b);k(c.screens,function(a){b>a?(c.screensCss.gt&&f("gt-"+a),c.screensCss.gte&&f("gte-"+
a)):b<a?(c.screensCss.lt&&f("lt-"+a),c.screensCss.lte&&f("lte-"+a)):b===a&&(c.screensCss.lte&&f("lte-"+a),c.screensCss.eq&&f("e-q"+a),c.screensCss.gte&&f("gte-"+a))});var d=a.innerHeight||q.clientHeight,g=a.outerHeight||a.screen.height;h.screen.innerHeight=d;h.screen.outerHeight=g;h.feature("portrait",d>b);h.feature("landscape",d<b)}function r(){a.clearTimeout(u);u=a.setTimeout(s,100)}var n=a.document,g=a.navigator,t=a.location,q=n.documentElement,p=[],c={screens:[240,320,480,640,768,800,1024,1280,
1440,1680,1920],screensCss:{gt:!0,gte:!1,lt:!0,lte:!1,eq:!1},browsers:[{ie:{min:6,max:10}}],browserCss:{gt:!0,gte:!1,lt:!0,lte:!1,eq:!0},section:"-section",page:"-page",head:"head"};if(a.head_conf)for(var b in a.head_conf)a.head_conf[b]!==w&&(c[b]=a.head_conf[b]);var h=a[c.head]=function(){h.ready.apply(null,arguments)};h.feature=function(a,b,c){if(!a)return q.className+=" "+p.join(" "),p=[],h;"[object Function]"===Object.prototype.toString.call(b)&&(b=b.call());f((b?"":"no-")+a);h[a]=!!b;c||(m("no-"+
a),m(a),h.feature());return h};h.feature("js",!0);b=g.userAgent.toLowerCase();g=/mobile|midp/.test(b);h.feature("mobile",g,!0);h.feature("desktop",!g,!0);b=/(chrome|firefox)[ \/]([\w.]+)/.exec(b)||/(iphone|ipad|ipod)(?:.*version)?[ \/]([\w.]+)/.exec(b)||/(android)(?:.*version)?[ \/]([\w.]+)/.exec(b)||/(webkit|opera)(?:.*version)?[ \/]([\w.]+)/.exec(b)||/(msie) ([\w.]+)/.exec(b)||[];g=b[1];b=parseFloat(b[2]);switch(g){case "msie":g="ie";b=n.documentMode||b;break;case "firefox":g="ff";break;case "ipod":case "ipad":case "iphone":g=
"ios";break;case "webkit":g="safari"}h.browser={name:g,version:b};h.browser[g]=!0;for(var v=0,x=c.browsers.length;v<x;v++)for(var i in c.browsers[v])if(g===i){f(i);for(var A=c.browsers[v][i].max,l=c.browsers[v][i].min;l<=A;l++)b>l?(c.browserCss.gt&&f("gt-"+i+l),c.browserCss.gte&&f("gte-"+i+l)):b<l?(c.browserCss.lt&&f("lt-"+i+l),c.browserCss.lte&&f("lte-"+i+l)):b===l&&(c.browserCss.lte&&f("lte-"+i+l),c.browserCss.eq&&f("eq-"+i+l),c.browserCss.gte&&f("gte-"+i+l))}else f("no-"+i);"ie"===g&&9>b&&k("abbr article aside audio canvas details figcaption figure footer header hgroup mark meter nav output progress section summary time video".split(" "),
function(a){n.createElement(a)});k(t.pathname.split("/"),function(a,b){if(2<this.length&&this[b+1]!==w)b&&f(this.slice(1,b+1).join("-").toLowerCase()+c.section);else{var g=a||"index",h=g.indexOf(".");0<h&&(g=g.substring(0,h));q.id=g.toLowerCase()+c.page;b||f("root"+c.section)}});h.screen={height:a.screen.height,width:a.screen.width};s();var u=0;a.addEventListener?a.addEventListener("resize",r,!1):a.attachEvent("onresize",r)})(window);
(function(a,w){function f(a){var f=a.charAt(0).toUpperCase()+a.substr(1),a=(a+" "+r.join(f+" ")+f).split(" "),c;a:{for(c in a)if(k[a[c]]!==w){c=!0;break a}c=!1}return!!c}var m=a.document.createElement("i"),k=m.style,s=" -o- -moz- -ms- -webkit- -khtml- ".split(" "),r=["Webkit","Moz","O","ms","Khtml"],n=a[a.head_conf&&a.head_conf.head||"head"],g={gradient:function(){k.cssText=("background-image:"+s.join("gradient(linear,left top,right bottom,from(#9f9),to(#fff));background-image:")+s.join("linear-gradient(left top,#eee,#fff);background-image:")).slice(0,
-17);return!!k.backgroundImage},rgba:function(){k.cssText="background-color:rgba(0,0,0,0.5)";return!!k.backgroundColor},opacity:function(){return""===m.style.opacity},textshadow:function(){return""===k.textShadow},multiplebgs:function(){k.cssText="background:url(//:),url(//:),red url(//:)";return/(url\s*\(.*?){3}/.test(k.background)},boxshadow:function(){return f("boxShadow")},borderimage:function(){return f("borderImage")},borderradius:function(){return f("borderRadius")},cssreflections:function(){return f("boxReflect")},
csstransforms:function(){return f("transform")},csstransitions:function(){return f("transition")},touch:function(){return"ontouchstart"in a},retina:function(){return 1<a.devicePixelRatio},fontface:function(){var a=n.browser.version;switch(n.browser.name){case "ie":return 9<=a;case "chrome":return 13<=a;case "ff":return 6<=a;case "ios":return 5<=a;case "android":return!1;case "webkit":return 5.1<=a;case "opera":return 10<=a;default:return!1}}},t;for(t in g)g[t]&&n.feature(t,g[t].call(),!0);n.feature()})(window);
(function(a,w){function f(){}function m(j,a){if(j){"object"===typeof j&&(j=[].slice.call(j));for(var b=0,c=j.length;b<c;b++)a.call(j,j[b],b)}}function k(a,b){var e=Object.prototype.toString.call(b).slice(8,-1);return b!==w&&null!==b&&e===a}function s(a){return k("Function",a)}function r(a){a=a||f;a._done||(a(),a._done=1)}function n(a){var b={};if("object"===typeof a)for(var e in a)a[e]&&(b={name:e,url:a[e]});else b=a.split("/"),b=b[b.length-1],e=b.indexOf("?"),b={name:-1!==e?b.substring(0,e):b,url:a};
return(a=i[b.name])&&a.url===b.url?a:i[b.name]=b}function g(a){var a=a||i,b;for(b in a)if(a.hasOwnProperty(b)&&a[b].state!==y)return!1;return!0}function t(a,b){b=b||f;a.state===y?b():a.state===D?d.ready(a.name,b):a.state===C?a.onpreload.push(function(){t(a,b)}):(a.state=D,q(a,function(){a.state=y;b();m(x[a.name],function(a){r(a)});u&&g()&&m(x.ALL,function(a){r(a)})}))}function q(j,c){var c=c||f,e;/\.css[^\.]*$/.test(j.url)?(e=b.createElement("link"),e.type="text/"+(j.type||"css"),e.rel="stylesheet",
e.href=j.url):(e=b.createElement("script"),e.type="text/"+(j.type||"javascript"),e.src=j.url);e.onload=e.onreadystatechange=function(j){j=j||a.event;if("load"===j.type||/loaded|complete/.test(e.readyState)&&(!b.documentMode||9>b.documentMode))e.onload=e.onreadystatechange=e.onerror=null,c()};e.onerror=function(){e.onload=e.onreadystatechange=e.onerror=null;c()};e.async=!1;e.defer=!1;var d=b.head||b.getElementsByTagName("head")[0];d.insertBefore(e,d.lastChild)}function p(){b.body?u||(u=!0,m(h,function(a){r(a)})):
(a.clearTimeout(d.readyTimeout),d.readyTimeout=a.setTimeout(p,50))}function c(){b.addEventListener?(b.removeEventListener("DOMContentLoaded",c,!1),p()):"complete"===b.readyState&&(b.detachEvent("onreadystatechange",c),p())}var b=a.document,h=[],v=[],x={},i={},A="async"in b.createElement("script")||"MozAppearance"in b.documentElement.style||a.opera,l,u,B=a.head_conf&&a.head_conf.head||"head",d=a[B]=a[B]||function(){d.ready.apply(null,arguments)},C=1,D=3,y=4;d.load=A?function(){var a=arguments,b=a[a.length-
1],e={};s(b)||(b=null);m(a,function(c,d){c!==b&&(c=n(c),e[c.name]=c,t(c,b&&d===a.length-2?function(){g(e)&&r(b)}:null))});return d}:function(){var a=arguments,b=[].slice.call(a,1),c=b[0];if(!l)return v.push(function(){d.load.apply(null,a)}),d;c?(m(b,function(a){if(!s(a)){var b=n(a);b.state===w&&(b.state=C,b.onpreload=[],q({url:b.url,type:"cache"},function(){b.state=2;m(b.onpreload,function(a){a.call()})}))}}),t(n(a[0]),s(c)?c:function(){d.load.apply(null,b)})):t(n(a[0]));return d};d.js=d.load;d.test=
function(a,b,c,g){a="object"===typeof a?a:{test:a,success:b?k("Array",b)?b:[b]:!1,failure:c?k("Array",c)?c:[c]:!1,callback:g||f};(b=!!a.test)&&a.success?(a.success.push(a.callback),d.load.apply(null,a.success)):!b&&a.failure?(a.failure.push(a.callback),d.load.apply(null,a.failure)):g();return d};d.ready=function(a,c){if(a===b)return u?r(c):h.push(c),d;s(a)&&(c=a,a="ALL");if("string"!==typeof a||!s(c))return d;var e=i[a];if(e&&e.state===y||"ALL"===a&&g()&&u)return r(c),d;(e=x[a])?e.push(c):x[a]=[c];
return d};d.ready(b,function(){g()&&m(x.ALL,function(a){r(a)});d.feature&&d.feature("domloaded",!0)});if("complete"===b.readyState)p();else if(b.addEventListener)b.addEventListener("DOMContentLoaded",c,!1),a.addEventListener("load",p,!1);else{b.attachEvent("onreadystatechange",c);a.attachEvent("onload",p);var z=!1;try{z=null==a.frameElement&&b.documentElement}catch(F){}z&&z.doScroll&&function E(){if(!u){try{z.doScroll("left")}catch(b){a.clearTimeout(d.readyTimeout);d.readyTimeout=a.setTimeout(E,50);
return}p()}}()}setTimeout(function(){l=!0;m(v,function(a){a()})},300)})(window);
epGlobal.head = head;
/* Modernizr 2.6.2 (Custom Build) | MIT & BSD
 * Build: http://modernizr.com/download/#-touch-teststyles-testprop-testallprops-hasevent-prefixes-domprefixes-css_scrollbars-load
 */
;window.Modernizr=function(a,b,c){function z(a){i.cssText=a}function A(a,b){return z(l.join(a+";")+(b||""))}function B(a,b){return typeof a===b}function C(a,b){return!!~(""+a).indexOf(b)}function D(a,b){for(var d in a){var e=a[d];if(!C(e,"-")&&i[e]!==c)return b=="pfx"?e:!0}return!1}function E(a,b,d){for(var e in a){var f=b[a[e]];if(f!==c)return d===!1?a[e]:B(f,"function")?f.bind(d||b):f}return!1}function F(a,b,c){var d=a.charAt(0).toUpperCase()+a.slice(1),e=(a+" "+n.join(d+" ")+d).split(" ");return B(b,"string")||B(b,"undefined")?D(e,b):(e=(a+" "+o.join(d+" ")+d).split(" "),E(e,b,c))}var d="2.6.2",e={},f=b.documentElement,g="modernizr",h=b.createElement(g),i=h.style,j,k={}.toString,l=" -webkit- -moz- -o- -ms- ".split(" "),m="Webkit Moz O ms",n=m.split(" "),o=m.toLowerCase().split(" "),p={},q={},r={},s=[],t=s.slice,u,v=function(a,c,d,e){var h,i,j,k,l=b.createElement("div"),m=b.body,n=m||b.createElement("body");if(parseInt(d,10))while(d--)j=b.createElement("div"),j.id=e?e[d]:g+(d+1),l.appendChild(j);return h=["&#173;",'<style id="s',g,'">',a,"</style>"].join(""),l.id=g,(m?l:n).innerHTML+=h,n.appendChild(l),m||(n.style.background="",n.style.overflow="hidden",k=f.style.overflow,f.style.overflow="hidden",f.appendChild(n)),i=c(l,a),m?l.parentNode.removeChild(l):(n.parentNode.removeChild(n),f.style.overflow=k),!!i},w=function(){function d(d,e){e=e||b.createElement(a[d]||"div"),d="on"+d;var f=d in e;return f||(e.setAttribute||(e=b.createElement("div")),e.setAttribute&&e.removeAttribute&&(e.setAttribute(d,""),f=B(e[d],"function"),B(e[d],"undefined")||(e[d]=c),e.removeAttribute(d))),e=null,f}var a={select:"input",change:"input",submit:"form",reset:"form",error:"img",load:"img",abort:"img"};return d}(),x={}.hasOwnProperty,y;!B(x,"undefined")&&!B(x.call,"undefined")?y=function(a,b){return x.call(a,b)}:y=function(a,b){return b in a&&B(a.constructor.prototype[b],"undefined")},Function.prototype.bind||(Function.prototype.bind=function(b){var c=this;if(typeof c!="function")throw new TypeError;var d=t.call(arguments,1),e=function(){if(this instanceof e){var a=function(){};a.prototype=c.prototype;var f=new a,g=c.apply(f,d.concat(t.call(arguments)));return Object(g)===g?g:f}return c.apply(b,d.concat(t.call(arguments)))};return e}),p.touch=function(){var c;return"ontouchstart"in a||a.DocumentTouch&&b instanceof DocumentTouch?c=!0:v(["@media (",l.join("touch-enabled),("),g,")","{#modernizr{top:9px;position:absolute}}"].join(""),function(a){c=a.offsetTop===9}),c};for(var G in p)y(p,G)&&(u=G.toLowerCase(),e[u]=p[G](),s.push((e[u]?"":"no-")+u));return e.addTest=function(a,b){if(typeof a=="object")for(var d in a)y(a,d)&&e.addTest(d,a[d]);else{a=a.toLowerCase();if(e[a]!==c)return e;b=typeof b=="function"?b():b,typeof enableClasses!="undefined"&&enableClasses&&(f.className+=" "+(b?"":"no-")+a),e[a]=b}return e},z(""),h=j=null,e._version=d,e._prefixes=l,e._domPrefixes=o,e._cssomPrefixes=n,e.hasEvent=w,e.testProp=function(a){return D([a])},e.testAllProps=F,e.testStyles=v,e}(this,this.document),function(a,b,c){function d(a){return"[object Function]"==o.call(a)}function e(a){return"string"==typeof a}function f(){}function g(a){return!a||"loaded"==a||"complete"==a||"uninitialized"==a}function h(){var a=p.shift();q=1,a?a.t?m(function(){("c"==a.t?B.injectCss:B.injectJs)(a.s,0,a.a,a.x,a.e,1)},0):(a(),h()):q=0}function i(a,c,d,e,f,i,j){function k(b){if(!o&&g(l.readyState)&&(u.r=o=1,!q&&h(),l.onload=l.onreadystatechange=null,b)){"img"!=a&&m(function(){t.removeChild(l)},50);for(var d in y[c])y[c].hasOwnProperty(d)&&y[c][d].onload()}}var j=j||B.errorTimeout,l=b.createElement(a),o=0,r=0,u={t:d,s:c,e:f,a:i,x:j};1===y[c]&&(r=1,y[c]=[]),"object"==a?l.data=c:(l.src=c,l.type=a),l.width=l.height="0",l.onerror=l.onload=l.onreadystatechange=function(){k.call(this,r)},p.splice(e,0,u),"img"!=a&&(r||2===y[c]?(t.insertBefore(l,s?null:n),m(k,j)):y[c].push(l))}function j(a,b,c,d,f){return q=0,b=b||"j",e(a)?i("c"==b?v:u,a,b,this.i++,c,d,f):(p.splice(this.i++,0,a),1==p.length&&h()),this}function k(){var a=B;return a.loader={load:j,i:0},a}var l=b.documentElement,m=a.setTimeout,n=b.getElementsByTagName("script")[0],o={}.toString,p=[],q=0,r="MozAppearance"in l.style,s=r&&!!b.createRange().compareNode,t=s?l:n.parentNode,l=a.opera&&"[object Opera]"==o.call(a.opera),l=!!b.attachEvent&&!l,u=r?"object":l?"script":"img",v=l?"script":u,w=Array.isArray||function(a){return"[object Array]"==o.call(a)},x=[],y={},z={timeout:function(a,b){return b.length&&(a.timeout=b[0]),a}},A,B;B=function(a){function b(a){var a=a.split("!"),b=x.length,c=a.pop(),d=a.length,c={url:c,origUrl:c,prefixes:a},e,f,g;for(f=0;f<d;f++)g=a[f].split("="),(e=z[g.shift()])&&(c=e(c,g));for(f=0;f<b;f++)c=x[f](c);return c}function g(a,e,f,g,h){var i=b(a),j=i.autoCallback;i.url.split(".").pop().split("?").shift(),i.bypass||(e&&(e=d(e)?e:e[a]||e[g]||e[a.split("/").pop().split("?")[0]]),i.instead?i.instead(a,e,f,g,h):(y[i.url]?i.noexec=!0:y[i.url]=1,f.load(i.url,i.forceCSS||!i.forceJS&&"css"==i.url.split(".").pop().split("?").shift()?"c":c,i.noexec,i.attrs,i.timeout),(d(e)||d(j))&&f.load(function(){k(),e&&e(i.origUrl,h,g),j&&j(i.origUrl,h,g),y[i.url]=2})))}function h(a,b){function c(a,c){if(a){if(e(a))c||(j=function(){var a=[].slice.call(arguments);k.apply(this,a),l()}),g(a,j,b,0,h);else if(Object(a)===a)for(n in m=function(){var b=0,c;for(c in a)a.hasOwnProperty(c)&&b++;return b}(),a)a.hasOwnProperty(n)&&(!c&&!--m&&(d(j)?j=function(){var a=[].slice.call(arguments);k.apply(this,a),l()}:j[n]=function(a){return function(){var b=[].slice.call(arguments);a&&a.apply(this,b),l()}}(k[n])),g(a[n],j,b,n,h))}else!c&&l()}var h=!!a.test,i=a.load||a.both,j=a.callback||f,k=j,l=a.complete||f,m,n;c(h?a.yep:a.nope,!!i),i&&c(i)}var i,j,l=this.yepnope.loader;if(e(a))g(a,0,l,0);else if(w(a))for(i=0;i<a.length;i++)j=a[i],e(j)?g(j,0,l,0):w(j)?B(j):Object(j)===j&&h(j,l);else Object(a)===a&&h(a,l)},B.addPrefix=function(a,b){z[a]=b},B.addFilter=function(a){x.push(a)},B.errorTimeout=1e4,null==b.readyState&&b.addEventListener&&(b.readyState="loading",b.addEventListener("DOMContentLoaded",A=function(){b.removeEventListener("DOMContentLoaded",A,0),b.readyState="complete"},0)),a.yepnope=k(),a.yepnope.executeStack=h,a.yepnope.injectJs=function(a,c,d,e,i,j){var k=b.createElement("script"),l,o,e=e||B.errorTimeout;k.src=a;for(o in d)k.setAttribute(o,d[o]);c=j?h:c||f,k.onreadystatechange=k.onload=function(){!l&&g(k.readyState)&&(l=1,c(),k.onload=k.onreadystatechange=null)},m(function(){l||(l=1,c(1))},e),i?k.onload():n.parentNode.insertBefore(k,n)},a.yepnope.injectCss=function(a,c,d,e,g,i){var e=b.createElement("link"),j,c=i?h:c||f;e.href=a,e.rel="stylesheet",e.type="text/css";for(j in d)e.setAttribute(j,d[j]);g||(n.parentNode.insertBefore(e,n),m(c,0))}}(this,document),Modernizr.load=function(){yepnope.apply(window,[].slice.call(arguments,0))},Modernizr.addTest("cssscrollbar",function(){var a,b="#modernizr{overflow: scroll; width: 40px }#"+Modernizr._prefixes.join("scrollbar{width:0px} #modernizr::").split("#").slice(1).join("#")+"scrollbar{width:0px}";return Modernizr.testStyles(b,function(b){a="scrollWidth"in b&&b.scrollWidth==40}),a});
/*! epGlobal.jQuery UI - v1.10.3 - 2013-07-19
* http://jqueryui.com
* Includes: jquery.ui.effect.js, jquery.ui.effect-blind.js, jquery.ui.effect-bounce.js, jquery.ui.effect-clip.js, jquery.ui.effect-drop.js, jquery.ui.effect-explode.js, jquery.ui.effect-fade.js, jquery.ui.effect-fold.js, jquery.ui.effect-highlight.js, jquery.ui.effect-pulsate.js, jquery.ui.effect-scale.js, jquery.ui.effect-shake.js, jquery.ui.effect-slide.js, jquery.ui.effect-transfer.js
* Copyright 2013 epGlobal.jQuery Foundation and other contributors Licensed MIT */

(function(t,e){var i="ui-effects-";t.effects={effect:{}},function(t,e){function i(t,e,i){var s=u[e.type]||{};return null==t?i||!e.def?null:e.def:(t=s.floor?~~t:parseFloat(t),isNaN(t)?e.def:s.mod?(t+s.mod)%s.mod:0>t?0:t>s.max?s.max:t)}function s(i){var s=l(),n=s._rgba=[];return i=i.toLowerCase(),f(h,function(t,a){var o,r=a.re.exec(i),h=r&&a.parse(r),l=a.space||"rgba";return h?(o=s[l](h),s[c[l].cache]=o[c[l].cache],n=s._rgba=o._rgba,!1):e}),n.length?("0,0,0,0"===n.join()&&t.extend(n,a.transparent),s):a[i]}function n(t,e,i){return i=(i+1)%1,1>6*i?t+6*(e-t)*i:1>2*i?e:2>3*i?t+6*(e-t)*(2/3-i):t}var a,o="backgroundColor borderBottomColor borderLeftColor borderRightColor borderTopColor color columnRuleColor outlineColor textDecorationColor textEmphasisColor",r=/^([\-+])=\s*(\d+\.?\d*)/,h=[{re:/rgba?\(\s*(\d{1,3})\s*,\s*(\d{1,3})\s*,\s*(\d{1,3})\s*(?:,\s*(\d?(?:\.\d+)?)\s*)?\)/,parse:function(t){return[t[1],t[2],t[3],t[4]]}},{re:/rgba?\(\s*(\d+(?:\.\d+)?)\%\s*,\s*(\d+(?:\.\d+)?)\%\s*,\s*(\d+(?:\.\d+)?)\%\s*(?:,\s*(\d?(?:\.\d+)?)\s*)?\)/,parse:function(t){return[2.55*t[1],2.55*t[2],2.55*t[3],t[4]]}},{re:/#([a-f0-9]{2})([a-f0-9]{2})([a-f0-9]{2})/,parse:function(t){return[parseInt(t[1],16),parseInt(t[2],16),parseInt(t[3],16)]}},{re:/#([a-f0-9])([a-f0-9])([a-f0-9])/,parse:function(t){return[parseInt(t[1]+t[1],16),parseInt(t[2]+t[2],16),parseInt(t[3]+t[3],16)]}},{re:/hsla?\(\s*(\d+(?:\.\d+)?)\s*,\s*(\d+(?:\.\d+)?)\%\s*,\s*(\d+(?:\.\d+)?)\%\s*(?:,\s*(\d?(?:\.\d+)?)\s*)?\)/,space:"hsla",parse:function(t){return[t[1],t[2]/100,t[3]/100,t[4]]}}],l=t.Color=function(e,i,s,n){return new t.Color.fn.parse(e,i,s,n)},c={rgba:{props:{red:{idx:0,type:"byte"},green:{idx:1,type:"byte"},blue:{idx:2,type:"byte"}}},hsla:{props:{hue:{idx:0,type:"degrees"},saturation:{idx:1,type:"percent"},lightness:{idx:2,type:"percent"}}}},u={"byte":{floor:!0,max:255},percent:{max:1},degrees:{mod:360,floor:!0}},d=l.support={},p=t("<p>")[0],f=t.each;p.style.cssText="background-color:rgba(1,1,1,.5)",d.rgba=p.style.backgroundColor.indexOf("rgba")>-1,f(c,function(t,e){e.cache="_"+t,e.props.alpha={idx:3,type:"percent",def:1}}),l.fn=t.extend(l.prototype,{parse:function(n,o,r,h){if(n===e)return this._rgba=[null,null,null,null],this;(n.jquery||n.nodeType)&&(n=t(n).css(o),o=e);var u=this,d=t.type(n),p=this._rgba=[];return o!==e&&(n=[n,o,r,h],d="array"),"string"===d?this.parse(s(n)||a._default):"array"===d?(f(c.rgba.props,function(t,e){p[e.idx]=i(n[e.idx],e)}),this):"object"===d?(n instanceof l?f(c,function(t,e){n[e.cache]&&(u[e.cache]=n[e.cache].slice())}):f(c,function(e,s){var a=s.cache;f(s.props,function(t,e){if(!u[a]&&s.to){if("alpha"===t||null==n[t])return;u[a]=s.to(u._rgba)}u[a][e.idx]=i(n[t],e,!0)}),u[a]&&0>t.inArray(null,u[a].slice(0,3))&&(u[a][3]=1,s.from&&(u._rgba=s.from(u[a])))}),this):e},is:function(t){var i=l(t),s=!0,n=this;return f(c,function(t,a){var o,r=i[a.cache];return r&&(o=n[a.cache]||a.to&&a.to(n._rgba)||[],f(a.props,function(t,i){return null!=r[i.idx]?s=r[i.idx]===o[i.idx]:e})),s}),s},_space:function(){var t=[],e=this;return f(c,function(i,s){e[s.cache]&&t.push(i)}),t.pop()},transition:function(t,e){var s=l(t),n=s._space(),a=c[n],o=0===this.alpha()?l("transparent"):this,r=o[a.cache]||a.to(o._rgba),h=r.slice();return s=s[a.cache],f(a.props,function(t,n){var a=n.idx,o=r[a],l=s[a],c=u[n.type]||{};null!==l&&(null===o?h[a]=l:(c.mod&&(l-o>c.mod/2?o+=c.mod:o-l>c.mod/2&&(o-=c.mod)),h[a]=i((l-o)*e+o,n)))}),this[n](h)},blend:function(e){if(1===this._rgba[3])return this;var i=this._rgba.slice(),s=i.pop(),n=l(e)._rgba;return l(t.map(i,function(t,e){return(1-s)*n[e]+s*t}))},toRgbaString:function(){var e="rgba(",i=t.map(this._rgba,function(t,e){return null==t?e>2?1:0:t});return 1===i[3]&&(i.pop(),e="rgb("),e+i.join()+")"},toHslaString:function(){var e="hsla(",i=t.map(this.hsla(),function(t,e){return null==t&&(t=e>2?1:0),e&&3>e&&(t=Math.round(100*t)+"%"),t});return 1===i[3]&&(i.pop(),e="hsl("),e+i.join()+")"},toHexString:function(e){var i=this._rgba.slice(),s=i.pop();return e&&i.push(~~(255*s)),"#"+t.map(i,function(t){return t=(t||0).toString(16),1===t.length?"0"+t:t}).join("")},toString:function(){return 0===this._rgba[3]?"transparent":this.toRgbaString()}}),l.fn.parse.prototype=l.fn,c.hsla.to=function(t){if(null==t[0]||null==t[1]||null==t[2])return[null,null,null,t[3]];var e,i,s=t[0]/255,n=t[1]/255,a=t[2]/255,o=t[3],r=Math.max(s,n,a),h=Math.min(s,n,a),l=r-h,c=r+h,u=.5*c;return e=h===r?0:s===r?60*(n-a)/l+360:n===r?60*(a-s)/l+120:60*(s-n)/l+240,i=0===l?0:.5>=u?l/c:l/(2-c),[Math.round(e)%360,i,u,null==o?1:o]},c.hsla.from=function(t){if(null==t[0]||null==t[1]||null==t[2])return[null,null,null,t[3]];var e=t[0]/360,i=t[1],s=t[2],a=t[3],o=.5>=s?s*(1+i):s+i-s*i,r=2*s-o;return[Math.round(255*n(r,o,e+1/3)),Math.round(255*n(r,o,e)),Math.round(255*n(r,o,e-1/3)),a]},f(c,function(s,n){var a=n.props,o=n.cache,h=n.to,c=n.from;l.fn[s]=function(s){if(h&&!this[o]&&(this[o]=h(this._rgba)),s===e)return this[o].slice();var n,r=t.type(s),u="array"===r||"object"===r?s:arguments,d=this[o].slice();return f(a,function(t,e){var s=u["object"===r?t:e.idx];null==s&&(s=d[e.idx]),d[e.idx]=i(s,e)}),c?(n=l(c(d)),n[o]=d,n):l(d)},f(a,function(e,i){l.fn[e]||(l.fn[e]=function(n){var a,o=t.type(n),h="alpha"===e?this._hsla?"hsla":"rgba":s,l=this[h](),c=l[i.idx];return"undefined"===o?c:("function"===o&&(n=n.call(this,c),o=t.type(n)),null==n&&i.empty?this:("string"===o&&(a=r.exec(n),a&&(n=c+parseFloat(a[2])*("+"===a[1]?1:-1))),l[i.idx]=n,this[h](l)))})})}),l.hook=function(e){var i=e.split(" ");f(i,function(e,i){t.cssHooks[i]={set:function(e,n){var a,o,r="";if("transparent"!==n&&("string"!==t.type(n)||(a=s(n)))){if(n=l(a||n),!d.rgba&&1!==n._rgba[3]){for(o="backgroundColor"===i?e.parentNode:e;(""===r||"transparent"===r)&&o&&o.style;)try{r=t.css(o,"backgroundColor"),o=o.parentNode}catch(h){}n=n.blend(r&&"transparent"!==r?r:"_default")}n=n.toRgbaString()}try{e.style[i]=n}catch(h){}}},t.fx.step[i]=function(e){e.colorInit||(e.start=l(e.elem,i),e.end=l(e.end),e.colorInit=!0),t.cssHooks[i].set(e.elem,e.start.transition(e.end,e.pos))}})},l.hook(o),t.cssHooks.borderColor={expand:function(t){var e={};return f(["Top","Right","Bottom","Left"],function(i,s){e["border"+s+"Color"]=t}),e}},a=t.Color.names={aqua:"#00ffff",black:"#000000",blue:"#0000ff",fuchsia:"#ff00ff",gray:"#808080",green:"#008000",lime:"#00ff00",maroon:"#800000",navy:"#000080",olive:"#808000",purple:"#800080",red:"#ff0000",silver:"#c0c0c0",teal:"#008080",white:"#ffffff",yellow:"#ffff00",transparent:[null,null,null,0],_default:"#ffffff"}}(epGlobal.jQuery),function(){function i(e){var i,s,n=e.ownerDocument.defaultView?e.ownerDocument.defaultView.getComputedStyle(e,null):e.currentStyle,a={};if(n&&n.length&&n[0]&&n[n[0]])for(s=n.length;s--;)i=n[s],"string"==typeof n[i]&&(a[t.camelCase(i)]=n[i]);else for(i in n)"string"==typeof n[i]&&(a[i]=n[i]);return a}function s(e,i){var s,n,o={};for(s in i)n=i[s],e[s]!==n&&(a[s]||(t.fx.step[s]||!isNaN(parseFloat(n)))&&(o[s]=n));return o}var n=["add","remove","toggle"],a={border:1,borderBottom:1,borderColor:1,borderLeft:1,borderRight:1,borderTop:1,borderWidth:1,margin:1,padding:1};t.each(["borderLeftStyle","borderRightStyle","borderBottomStyle","borderTopStyle"],function(e,i){t.fx.step[i]=function(t){("none"!==t.end&&!t.setAttr||1===t.pos&&!t.setAttr)&&(epGlobal.jQuery.style(t.elem,i,t.end),t.setAttr=!0)}}),t.fn.addBack||(t.fn.addBack=function(t){return this.add(null==t?this.prevObject:this.prevObject.filter(t))}),t.effects.animateClass=function(e,a,o,r){var h=t.speed(a,o,r);return this.queue(function(){var a,o=t(this),r=o.attr("class")||"",l=h.children?o.find("*").addBack():o;l=l.map(function(){var e=t(this);return{el:e,start:i(this)}}),a=function(){t.each(n,function(t,i){e[i]&&o[i+"Class"](e[i])})},a(),l=l.map(function(){return this.end=i(this.el[0]),this.diff=s(this.start,this.end),this}),o.attr("class",r),l=l.map(function(){var e=this,i=t.Deferred(),s=t.extend({},h,{queue:!1,complete:function(){i.resolve(e)}});return this.el.animate(this.diff,s),i.promise()}),t.when.apply(t,l.get()).done(function(){a(),t.each(arguments,function(){var e=this.el;t.each(this.diff,function(t){e.css(t,"")})}),h.complete.call(o[0])})})},t.fn.extend({addClass:function(e){return function(i,s,n,a){return s?t.effects.animateClass.call(this,{add:i},s,n,a):e.apply(this,arguments)}}(t.fn.addClass),removeClass:function(e){return function(i,s,n,a){return arguments.length>1?t.effects.animateClass.call(this,{remove:i},s,n,a):e.apply(this,arguments)}}(t.fn.removeClass),toggleClass:function(i){return function(s,n,a,o,r){return"boolean"==typeof n||n===e?a?t.effects.animateClass.call(this,n?{add:s}:{remove:s},a,o,r):i.apply(this,arguments):t.effects.animateClass.call(this,{toggle:s},n,a,o)}}(t.fn.toggleClass),switchClass:function(e,i,s,n,a){return t.effects.animateClass.call(this,{add:i,remove:e},s,n,a)}})}(),function(){function s(e,i,s,n){return t.isPlainObject(e)&&(i=e,e=e.effect),e={effect:e},null==i&&(i={}),t.isFunction(i)&&(n=i,s=null,i={}),("number"==typeof i||t.fx.speeds[i])&&(n=s,s=i,i={}),t.isFunction(s)&&(n=s,s=null),i&&t.extend(e,i),s=s||i.duration,e.duration=t.fx.off?0:"number"==typeof s?s:s in t.fx.speeds?t.fx.speeds[s]:t.fx.speeds._default,e.complete=n||i.complete,e}function n(e){return!e||"number"==typeof e||t.fx.speeds[e]?!0:"string"!=typeof e||t.effects.effect[e]?t.isFunction(e)?!0:"object"!=typeof e||e.effect?!1:!0:!0}t.extend(t.effects,{version:"1.10.3",save:function(t,e){for(var s=0;e.length>s;s++)null!==e[s]&&t.data(i+e[s],t[0].style[e[s]])},restore:function(t,s){var n,a;for(a=0;s.length>a;a++)null!==s[a]&&(n=t.data(i+s[a]),n===e&&(n=""),t.css(s[a],n))},setMode:function(t,e){return"toggle"===e&&(e=t.is(":hidden")?"show":"hide"),e},getBaseline:function(t,e){var i,s;switch(t[0]){case"top":i=0;break;case"middle":i=.5;break;case"bottom":i=1;break;default:i=t[0]/e.height}switch(t[1]){case"left":s=0;break;case"center":s=.5;break;case"right":s=1;break;default:s=t[1]/e.width}return{x:s,y:i}},createWrapper:function(e){if(e.parent().is(".ui-effects-wrapper"))return e.parent();var i={width:e.outerWidth(!0),height:e.outerHeight(!0),"float":e.css("float")},s=t("<div></div>").addClass("ui-effects-wrapper").css({fontSize:"100%",background:"transparent",border:"none",margin:0,padding:0}),n={width:e.width(),height:e.height()},a=document.activeElement;try{a.id}catch(o){a=document.body}return e.wrap(s),(e[0]===a||t.contains(e[0],a))&&t(a).focus(),s=e.parent(),"static"===e.css("position")?(s.css({position:"relative"}),e.css({position:"relative"})):(t.extend(i,{position:e.css("position"),zIndex:e.css("z-index")}),t.each(["top","left","bottom","right"],function(t,s){i[s]=e.css(s),isNaN(parseInt(i[s],10))&&(i[s]="auto")}),e.css({position:"relative",top:0,left:0,right:"auto",bottom:"auto"})),e.css(n),s.css(i).show()},removeWrapper:function(e){var i=document.activeElement;return e.parent().is(".ui-effects-wrapper")&&(e.parent().replaceWith(e),(e[0]===i||t.contains(e[0],i))&&t(i).focus()),e},setTransition:function(e,i,s,n){return n=n||{},t.each(i,function(t,i){var a=e.cssUnit(i);a[0]>0&&(n[i]=a[0]*s+a[1])}),n}}),t.fn.extend({effect:function(){function e(e){function s(){t.isFunction(a)&&a.call(n[0]),t.isFunction(e)&&e()}var n=t(this),a=i.complete,r=i.mode;(n.is(":hidden")?"hide"===r:"show"===r)?(n[r](),s()):o.call(n[0],i,s)}var i=s.apply(this,arguments),n=i.mode,a=i.queue,o=t.effects.effect[i.effect];return t.fx.off||!o?n?this[n](i.duration,i.complete):this.each(function(){i.complete&&i.complete.call(this)}):a===!1?this.each(e):this.queue(a||"fx",e)},show:function(t){return function(e){if(n(e))return t.apply(this,arguments);var i=s.apply(this,arguments);return i.mode="show",this.effect.call(this,i)}}(t.fn.show),hide:function(t){return function(e){if(n(e))return t.apply(this,arguments);var i=s.apply(this,arguments);return i.mode="hide",this.effect.call(this,i)}}(t.fn.hide),toggle:function(t){return function(e){if(n(e)||"boolean"==typeof e)return t.apply(this,arguments);var i=s.apply(this,arguments);return i.mode="toggle",this.effect.call(this,i)}}(t.fn.toggle),cssUnit:function(e){var i=this.css(e),s=[];return t.each(["em","px","%","pt"],function(t,e){i.indexOf(e)>0&&(s=[parseFloat(i),e])}),s}})}(),function(){var e={};t.each(["Quad","Cubic","Quart","Quint","Expo"],function(t,i){e[i]=function(e){return Math.pow(e,t+2)}}),t.extend(e,{Sine:function(t){return 1-Math.cos(t*Math.PI/2)},Circ:function(t){return 1-Math.sqrt(1-t*t)},Elastic:function(t){return 0===t||1===t?t:-Math.pow(2,8*(t-1))*Math.sin((80*(t-1)-7.5)*Math.PI/15)},Back:function(t){return t*t*(3*t-2)},Bounce:function(t){for(var e,i=4;((e=Math.pow(2,--i))-1)/11>t;);return 1/Math.pow(4,3-i)-7.5625*Math.pow((3*e-2)/22-t,2)}}),t.each(e,function(e,i){t.easing["easeIn"+e]=i,t.easing["easeOut"+e]=function(t){return 1-i(1-t)},t.easing["easeInOut"+e]=function(t){return.5>t?i(2*t)/2:1-i(-2*t+2)/2}})}()})(epGlobal.jQuery);(function(t){var e=/up|down|vertical/,i=/up|left|vertical|horizontal/;t.effects.effect.blind=function(s,n){var a,o,r,h=t(this),l=["position","top","bottom","left","right","height","width"],c=t.effects.setMode(h,s.mode||"hide"),u=s.direction||"up",d=e.test(u),p=d?"height":"width",f=d?"top":"left",m=i.test(u),g={},v="show"===c;h.parent().is(".ui-effects-wrapper")?t.effects.save(h.parent(),l):t.effects.save(h,l),h.show(),a=t.effects.createWrapper(h).css({overflow:"hidden"}),o=a[p](),r=parseFloat(a.css(f))||0,g[p]=v?o:0,m||(h.css(d?"bottom":"right",0).css(d?"top":"left","auto").css({position:"absolute"}),g[f]=v?r:o+r),v&&(a.css(p,0),m||a.css(f,r+o)),a.animate(g,{duration:s.duration,easing:s.easing,queue:!1,complete:function(){"hide"===c&&h.hide(),t.effects.restore(h,l),t.effects.removeWrapper(h),n()}})}})(epGlobal.jQuery);(function(t){t.effects.effect.bounce=function(e,i){var s,n,a,o=t(this),r=["position","top","bottom","left","right","height","width"],h=t.effects.setMode(o,e.mode||"effect"),l="hide"===h,c="show"===h,u=e.direction||"up",d=e.distance,p=e.times||5,f=2*p+(c||l?1:0),m=e.duration/f,g=e.easing,v="up"===u||"down"===u?"top":"left",_="up"===u||"left"===u,b=o.queue(),y=b.length;for((c||l)&&r.push("opacity"),t.effects.save(o,r),o.show(),t.effects.createWrapper(o),d||(d=o["top"===v?"outerHeight":"outerWidth"]()/3),c&&(a={opacity:1},a[v]=0,o.css("opacity",0).css(v,_?2*-d:2*d).animate(a,m,g)),l&&(d/=Math.pow(2,p-1)),a={},a[v]=0,s=0;p>s;s++)n={},n[v]=(_?"-=":"+=")+d,o.animate(n,m,g).animate(a,m,g),d=l?2*d:d/2;l&&(n={opacity:0},n[v]=(_?"-=":"+=")+d,o.animate(n,m,g)),o.queue(function(){l&&o.hide(),t.effects.restore(o,r),t.effects.removeWrapper(o),i()}),y>1&&b.splice.apply(b,[1,0].concat(b.splice(y,f+1))),o.dequeue()}})(epGlobal.jQuery);(function(t){t.effects.effect.clip=function(e,i){var s,n,a,o=t(this),r=["position","top","bottom","left","right","height","width"],h=t.effects.setMode(o,e.mode||"hide"),l="show"===h,c=e.direction||"vertical",u="vertical"===c,d=u?"height":"width",p=u?"top":"left",f={};t.effects.save(o,r),o.show(),s=t.effects.createWrapper(o).css({overflow:"hidden"}),n="IMG"===o[0].tagName?s:o,a=n[d](),l&&(n.css(d,0),n.css(p,a/2)),f[d]=l?a:0,f[p]=l?0:a/2,n.animate(f,{queue:!1,duration:e.duration,easing:e.easing,complete:function(){l||o.hide(),t.effects.restore(o,r),t.effects.removeWrapper(o),i()}})}})(epGlobal.jQuery);(function(t){t.effects.effect.drop=function(e,i){var s,n=t(this),a=["position","top","bottom","left","right","opacity","height","width"],o=t.effects.setMode(n,e.mode||"hide"),r="show"===o,h=e.direction||"left",l="up"===h||"down"===h?"top":"left",c="up"===h||"left"===h?"pos":"neg",u={opacity:r?1:0};t.effects.save(n,a),n.show(),t.effects.createWrapper(n),s=e.distance||n["top"===l?"outerHeight":"outerWidth"](!0)/2,r&&n.css("opacity",0).css(l,"pos"===c?-s:s),u[l]=(r?"pos"===c?"+=":"-=":"pos"===c?"-=":"+=")+s,n.animate(u,{queue:!1,duration:e.duration,easing:e.easing,complete:function(){"hide"===o&&n.hide(),t.effects.restore(n,a),t.effects.removeWrapper(n),i()}})}})(epGlobal.jQuery);(function(t){t.effects.effect.explode=function(e,i){function s(){b.push(this),b.length===u*d&&n()}function n(){p.css({visibility:"visible"}),t(b).remove(),m||p.hide(),i()}var a,o,r,h,l,c,u=e.pieces?Math.round(Math.sqrt(e.pieces)):3,d=u,p=t(this),f=t.effects.setMode(p,e.mode||"hide"),m="show"===f,g=p.show().css("visibility","hidden").offset(),v=Math.ceil(p.outerWidth()/d),_=Math.ceil(p.outerHeight()/u),b=[];for(a=0;u>a;a++)for(h=g.top+a*_,c=a-(u-1)/2,o=0;d>o;o++)r=g.left+o*v,l=o-(d-1)/2,p.clone().appendTo("body").wrap("<div></div>").css({position:"absolute",visibility:"visible",left:-o*v,top:-a*_}).parent().addClass("ui-effects-explode").css({position:"absolute",overflow:"hidden",width:v,height:_,left:r+(m?l*v:0),top:h+(m?c*_:0),opacity:m?0:1}).animate({left:r+(m?0:l*v),top:h+(m?0:c*_),opacity:m?1:0},e.duration||500,e.easing,s)}})(epGlobal.jQuery);(function(t){t.effects.effect.fade=function(e,i){var s=t(this),n=t.effects.setMode(s,e.mode||"toggle");s.animate({opacity:n},{queue:!1,duration:e.duration,easing:e.easing,complete:i})}})(epGlobal.jQuery);(function(t){t.effects.effect.fold=function(e,i){var s,n,a=t(this),o=["position","top","bottom","left","right","height","width"],r=t.effects.setMode(a,e.mode||"hide"),h="show"===r,l="hide"===r,c=e.size||15,u=/([0-9]+)%/.exec(c),d=!!e.horizFirst,p=h!==d,f=p?["width","height"]:["height","width"],m=e.duration/2,g={},v={};t.effects.save(a,o),a.show(),s=t.effects.createWrapper(a).css({overflow:"hidden"}),n=p?[s.width(),s.height()]:[s.height(),s.width()],u&&(c=parseInt(u[1],10)/100*n[l?0:1]),h&&s.css(d?{height:0,width:c}:{height:c,width:0}),g[f[0]]=h?n[0]:c,v[f[1]]=h?n[1]:0,s.animate(g,m,e.easing).animate(v,m,e.easing,function(){l&&a.hide(),t.effects.restore(a,o),t.effects.removeWrapper(a),i()})}})(epGlobal.jQuery);(function(t){t.effects.effect.highlight=function(e,i){var s=t(this),n=["backgroundImage","backgroundColor","opacity"],a=t.effects.setMode(s,e.mode||"show"),o={backgroundColor:s.css("backgroundColor")};"hide"===a&&(o.opacity=0),t.effects.save(s,n),s.show().css({backgroundImage:"none",backgroundColor:e.color||"#ffff99"}).animate(o,{queue:!1,duration:e.duration,easing:e.easing,complete:function(){"hide"===a&&s.hide(),t.effects.restore(s,n),i()}})}})(epGlobal.jQuery);(function(t){t.effects.effect.pulsate=function(e,i){var s,n=t(this),a=t.effects.setMode(n,e.mode||"show"),o="show"===a,r="hide"===a,h=o||"hide"===a,l=2*(e.times||5)+(h?1:0),c=e.duration/l,u=0,d=n.queue(),p=d.length;for((o||!n.is(":visible"))&&(n.css("opacity",0).show(),u=1),s=1;l>s;s++)n.animate({opacity:u},c,e.easing),u=1-u;n.animate({opacity:u},c,e.easing),n.queue(function(){r&&n.hide(),i()}),p>1&&d.splice.apply(d,[1,0].concat(d.splice(p,l+1))),n.dequeue()}})(epGlobal.jQuery);(function(t){t.effects.effect.puff=function(e,i){var s=t(this),n=t.effects.setMode(s,e.mode||"hide"),a="hide"===n,o=parseInt(e.percent,10)||150,r=o/100,h={height:s.height(),width:s.width(),outerHeight:s.outerHeight(),outerWidth:s.outerWidth()};t.extend(e,{effect:"scale",queue:!1,fade:!0,mode:n,complete:i,percent:a?o:100,from:a?h:{height:h.height*r,width:h.width*r,outerHeight:h.outerHeight*r,outerWidth:h.outerWidth*r}}),s.effect(e)},t.effects.effect.scale=function(e,i){var s=t(this),n=t.extend(!0,{},e),a=t.effects.setMode(s,e.mode||"effect"),o=parseInt(e.percent,10)||(0===parseInt(e.percent,10)?0:"hide"===a?0:100),r=e.direction||"both",h=e.origin,l={height:s.height(),width:s.width(),outerHeight:s.outerHeight(),outerWidth:s.outerWidth()},c={y:"horizontal"!==r?o/100:1,x:"vertical"!==r?o/100:1};n.effect="size",n.queue=!1,n.complete=i,"effect"!==a&&(n.origin=h||["middle","center"],n.restore=!0),n.from=e.from||("show"===a?{height:0,width:0,outerHeight:0,outerWidth:0}:l),n.to={height:l.height*c.y,width:l.width*c.x,outerHeight:l.outerHeight*c.y,outerWidth:l.outerWidth*c.x},n.fade&&("show"===a&&(n.from.opacity=0,n.to.opacity=1),"hide"===a&&(n.from.opacity=1,n.to.opacity=0)),s.effect(n)},t.effects.effect.size=function(e,i){var s,n,a,o=t(this),r=["position","top","bottom","left","right","width","height","overflow","opacity"],h=["position","top","bottom","left","right","overflow","opacity"],l=["width","height","overflow"],c=["fontSize"],u=["borderTopWidth","borderBottomWidth","paddingTop","paddingBottom"],d=["borderLeftWidth","borderRightWidth","paddingLeft","paddingRight"],p=t.effects.setMode(o,e.mode||"effect"),f=e.restore||"effect"!==p,m=e.scale||"both",g=e.origin||["middle","center"],v=o.css("position"),_=f?r:h,b={height:0,width:0,outerHeight:0,outerWidth:0};"show"===p&&o.show(),s={height:o.height(),width:o.width(),outerHeight:o.outerHeight(),outerWidth:o.outerWidth()},"toggle"===e.mode&&"show"===p?(o.from=e.to||b,o.to=e.from||s):(o.from=e.from||("show"===p?b:s),o.to=e.to||("hide"===p?b:s)),a={from:{y:o.from.height/s.height,x:o.from.width/s.width},to:{y:o.to.height/s.height,x:o.to.width/s.width}},("box"===m||"both"===m)&&(a.from.y!==a.to.y&&(_=_.concat(u),o.from=t.effects.setTransition(o,u,a.from.y,o.from),o.to=t.effects.setTransition(o,u,a.to.y,o.to)),a.from.x!==a.to.x&&(_=_.concat(d),o.from=t.effects.setTransition(o,d,a.from.x,o.from),o.to=t.effects.setTransition(o,d,a.to.x,o.to))),("content"===m||"both"===m)&&a.from.y!==a.to.y&&(_=_.concat(c).concat(l),o.from=t.effects.setTransition(o,c,a.from.y,o.from),o.to=t.effects.setTransition(o,c,a.to.y,o.to)),t.effects.save(o,_),o.show(),t.effects.createWrapper(o),o.css("overflow","hidden").css(o.from),g&&(n=t.effects.getBaseline(g,s),o.from.top=(s.outerHeight-o.outerHeight())*n.y,o.from.left=(s.outerWidth-o.outerWidth())*n.x,o.to.top=(s.outerHeight-o.to.outerHeight)*n.y,o.to.left=(s.outerWidth-o.to.outerWidth)*n.x),o.css(o.from),("content"===m||"both"===m)&&(u=u.concat(["marginTop","marginBottom"]).concat(c),d=d.concat(["marginLeft","marginRight"]),l=r.concat(u).concat(d),o.find("*[width]").each(function(){var i=t(this),s={height:i.height(),width:i.width(),outerHeight:i.outerHeight(),outerWidth:i.outerWidth()};f&&t.effects.save(i,l),i.from={height:s.height*a.from.y,width:s.width*a.from.x,outerHeight:s.outerHeight*a.from.y,outerWidth:s.outerWidth*a.from.x},i.to={height:s.height*a.to.y,width:s.width*a.to.x,outerHeight:s.height*a.to.y,outerWidth:s.width*a.to.x},a.from.y!==a.to.y&&(i.from=t.effects.setTransition(i,u,a.from.y,i.from),i.to=t.effects.setTransition(i,u,a.to.y,i.to)),a.from.x!==a.to.x&&(i.from=t.effects.setTransition(i,d,a.from.x,i.from),i.to=t.effects.setTransition(i,d,a.to.x,i.to)),i.css(i.from),i.animate(i.to,e.duration,e.easing,function(){f&&t.effects.restore(i,l)})})),o.animate(o.to,{queue:!1,duration:e.duration,easing:e.easing,complete:function(){0===o.to.opacity&&o.css("opacity",o.from.opacity),"hide"===p&&o.hide(),t.effects.restore(o,_),f||("static"===v?o.css({position:"relative",top:o.to.top,left:o.to.left}):t.each(["top","left"],function(t,e){o.css(e,function(e,i){var s=parseInt(i,10),n=t?o.to.left:o.to.top;return"auto"===i?n+"px":s+n+"px"})})),t.effects.removeWrapper(o),i()}})}})(epGlobal.jQuery);(function(t){t.effects.effect.shake=function(e,i){var s,n=t(this),a=["position","top","bottom","left","right","height","width"],o=t.effects.setMode(n,e.mode||"effect"),r=e.direction||"left",h=e.distance||20,l=e.times||3,c=2*l+1,u=Math.round(e.duration/c),d="up"===r||"down"===r?"top":"left",p="up"===r||"left"===r,f={},m={},g={},v=n.queue(),_=v.length;for(t.effects.save(n,a),n.show(),t.effects.createWrapper(n),f[d]=(p?"-=":"+=")+h,m[d]=(p?"+=":"-=")+2*h,g[d]=(p?"-=":"+=")+2*h,n.animate(f,u,e.easing),s=1;l>s;s++)n.animate(m,u,e.easing).animate(g,u,e.easing);n.animate(m,u,e.easing).animate(f,u/2,e.easing).queue(function(){"hide"===o&&n.hide(),t.effects.restore(n,a),t.effects.removeWrapper(n),i()}),_>1&&v.splice.apply(v,[1,0].concat(v.splice(_,c+1))),n.dequeue()}})(epGlobal.jQuery);(function(t){t.effects.effect.slide=function(e,i){var s,n=t(this),a=["position","top","bottom","left","right","width","height"],o=t.effects.setMode(n,e.mode||"show"),r="show"===o,h=e.direction||"left",l="up"===h||"down"===h?"top":"left",c="up"===h||"left"===h,u={};t.effects.save(n,a),n.show(),s=e.distance||n["top"===l?"outerHeight":"outerWidth"](!0),t.effects.createWrapper(n).css({overflow:"hidden"}),r&&n.css(l,c?isNaN(s)?"-"+s:-s:s),u[l]=(r?c?"+=":"-=":c?"-=":"+=")+s,n.animate(u,{queue:!1,duration:e.duration,easing:e.easing,complete:function(){"hide"===o&&n.hide(),t.effects.restore(n,a),t.effects.removeWrapper(n),i()}})}})(epGlobal.jQuery);(function(t){t.effects.effect.transfer=function(e,i){var s=t(this),n=t(e.to),a="fixed"===n.css("position"),o=t("body"),r=a?o.scrollTop():0,h=a?o.scrollLeft():0,l=n.offset(),c={top:l.top-r,left:l.left-h,height:n.innerHeight(),width:n.innerWidth()},u=s.offset(),d=t("<div class='ui-effects-transfer'></div>").appendTo(document.body).addClass(e.className).css({top:u.top-r,left:u.left-h,height:s.innerHeight(),width:s.innerWidth(),position:a?"fixed":"absolute"}).animate(c,e.duration,e.easing,function(){d.remove(),i()})}})(epGlobal.jQuery);
/*!
 * NETEYE Activity Indicator jQuery Plugin
 *
 * Copyright (c) 2010 NETEYE GmbH
 * Licensed under the MIT license
 *
 * Author: Felix Gnass [fgnass at neteye dot de]
 * Version: 1.0.0
 */
 
/**
 * Plugin that renders a customisable activity indicator (spinner) using SVG or VML.
 */
(function($) {

	$.fn.activity = function(opts) {
		this.each(function() {
			var $this = $(this);
			var el = $this.data('activity');
			if (el) {
				clearInterval(el.data('interval'));
				el.remove();
				$this.removeData('activity');
			}
			if (opts !== false) {
				opts = $.extend({color: $this.css('color')}, $.fn.activity.defaults, opts);
				
				el = render($this, opts).css('position', 'absolute').prependTo(opts.outside ? 'body' : $this);
				var h = $this.outerHeight() - el.height();
				var w = $this.outerWidth() - el.width();
				var margin = {
					top: opts.valign == 'top' ? opts.padding : opts.valign == 'bottom' ? h - opts.padding : Math.floor(h / 2),
					left: opts.align == 'left' ? opts.padding : opts.align == 'right' ? w - opts.padding : Math.floor(w / 2)
				};
				var offset = $this.offset();
				if (opts.outside) {
					el.css({top: offset.top + 'px', left: offset.left + 'px'});
				}
				else {
					margin.top -= el.offset().top - offset.top;
					margin.left -= el.offset().left - offset.left;
				}
				el.css({marginTop: margin.top + 'px', marginLeft: margin.left + 'px'});
				animate(el, opts.segments, Math.round(10 / opts.speed) / 10);
				$this.data('activity', el);
			}
		});
		return this;
	};
	
	$.fn.activity.defaults = {
		segments: 12,
		space: 3,
		length: 7,
		width: 4,
		speed: 1.2,
		align: 'center',
		valign: 'center',
		padding: 4
	};
	
	$.fn.activity.getOpacity = function(opts, i) {
		var steps = opts.steps || opts.segments-1;
		var end = opts.opacity !== undefined ? opts.opacity : 1/steps;
		return 1 - Math.min(i, steps) * (1 - end) / steps;
	};
	
	/**
	 * Default rendering strategy. If neither SVG nor VML is available, a div with class-name 'busy' 
	 * is inserted, that can be styled with CSS to display an animated gif as fallback.
	 */
	var render = function() {
		return $('<div>').addClass('busy');
	};
	
	/**
	 * The default animation strategy does nothing as we expect an animated gif as fallback.
	 */
	var animate = function() {
	};
	
	/**
	 * Utility function to create elements in the SVG namespace.
	 */
	function svg(tag, attr) {
		var el = document.createElementNS("http://www.w3.org/2000/svg", tag || 'svg');
		if (attr) {
			$.each(attr, function(k, v) {
				el.setAttributeNS(null, k, v);
			});
		}
		return $(el);
	}
	
	if (document.createElementNS && document.createElementNS( "http://www.w3.org/2000/svg", "svg").createSVGRect) {
	
		// =======================================================================================
		// SVG Rendering
		// =======================================================================================
		
		/**
		 * Rendering strategy that creates a SVG tree.
		 */
		render = function(target, d) {
			var innerRadius = d.width*2 + d.space;
			var r = (innerRadius + d.length + Math.ceil(d.width / 2) + 1);
			
			var el = svg().width(r*2).height(r*2);
			
			var g = svg('g', {
				'stroke-width': d.width, 
				'stroke-linecap': 'round', 
				stroke: d.color
			}).appendTo(svg('g', {transform: 'translate('+ r +','+ r +')'}).appendTo(el));
			
			for (var i = 0; i < d.segments; i++) {
				g.append(svg('line', {
					x1: 0, 
					y1: innerRadius, 
					x2: 0, 
					y2: innerRadius + d.length, 
					transform: 'rotate(' + (360 / d.segments * i) + ', 0, 0)',
					opacity: $.fn.activity.getOpacity(d, i)
				}));
			}
			return $('<div>').append(el).width(2*r).height(2*r);
		};
				
		// Check if Webkit CSS animations are available, as they work much better on the iPad
		// than setTimeout() based animations.
		
		if (document.createElement('div').style.WebkitAnimationName !== undefined) {

			var animations = {};
		
			/**
			 * Animation strategy that uses dynamically created CSS animation rules.
			 */
			animate = function(el, steps, duration) {
				if (!animations[steps]) {
					var name = 'spin' + steps;
					var rule = '@-webkit-keyframes '+ name +' {';
					for (var i=0; i < steps; i++) {
						var p1 = Math.round(100000 / steps * i) / 1000;
						var p2 = Math.round(100000 / steps * (i+1) - 1) / 1000;
						var value = '% { -webkit-transform:rotate(' + Math.round(360 / steps * i) + 'deg); }\n';
						rule += p1 + value + p2 + value; 
					}
					rule += '100% { -webkit-transform:rotate(100deg); }\n}';
					document.styleSheets[0].insertRule(rule);
					animations[steps] = name;
				}
				el.css('-webkit-animation', animations[steps] + ' ' + duration +'s linear infinite');
			};
		}
		else {
		
			/**
			 * Animation strategy that transforms a SVG element using setInterval().
			 */
			animate = function(el, steps, duration) {
				var rotation = 0;
				var g = el.find('g g').get(0);
				el.data('interval', setInterval(function() {
					g.setAttributeNS(null, 'transform', 'rotate(' + (++rotation % steps * (360 / steps)) + ')');
				},  duration * 1000 / steps));
			};
		}
		
	}
	else {
		
		// =======================================================================================
		// VML Rendering
		// =======================================================================================
		
		var s = $('<shape>').css('behavior', 'url(#default#VML)').appendTo('body');
			
		if (s.get(0).adj) {
		
			// VML support detected. Insert CSS rules for group, shape and stroke.
			var sheet = document.createStyleSheet();
			$.each(['group', 'shape', 'stroke'], function() {
				sheet.addRule(this, "behavior:url(#default#VML);");
			});
			
			/**
			 * Rendering strategy that creates a VML tree. 
			 */
			render = function(target, d) {
			
				var innerRadius = d.width*2 + d.space;
				var r = (innerRadius + d.length + Math.ceil(d.width / 2) + 1);
				var s = r*2;
				var o = -Math.ceil(s/2);
				
				var el = $('<group>', {coordsize: s + ' ' + s, coordorigin: o + ' ' + o}).css({top: o, left: o, width: s, height: s});
				for (var i = 0; i < d.segments; i++) {
					el.append($('<shape>', {path: 'm ' + innerRadius + ',0  l ' + (innerRadius + d.length) + ',0'}).css({
						width: s,
						height: s,
						rotation: (360 / d.segments * i) + 'deg'
					}).append($('<stroke>', {color: d.color, weight: d.width + 'px', endcap: 'round', opacity: $.fn.activity.getOpacity(d, i)})));
				}
				return $('<group>', {coordsize: s + ' ' + s}).css({width: s, height: s, overflow: 'hidden'}).append(el);
			};
		
			/**
		     * Animation strategy that modifies the VML rotation property using setInterval().
		     */
			animate = function(el, steps, duration) {
				var rotation = 0;
				var g = el.get(0);
				el.data('interval', setInterval(function() {
					g.style.rotation = ++rotation % steps * (360 / steps);
				},  duration * 1000 / steps));
			};
		}
		$(s).remove();
	}

})(epGlobal.jQuery);

/*!
 * jQuery throttle / debounce - v1.1 - 3/7/2010
 * http://benalman.com/projects/jquery-throttle-debounce-plugin/
 * 
 * Copyright (c) 2010 "Cowboy" Ben Alman
 * Dual licensed under the MIT and GPL licenses.
 * http://benalman.com/about/license/
 */

// Script: jQuery throttle / debounce: Sometimes, less is more!
//
// *Version: 1.1, Last updated: 3/7/2010*
// 
// Project Home - http://benalman.com/projects/jquery-throttle-debounce-plugin/
// GitHub       - http://github.com/cowboy/jquery-throttle-debounce/
// Source       - http://github.com/cowboy/jquery-throttle-debounce/raw/master/jquery.ba-throttle-debounce.js
// (Minified)   - http://github.com/cowboy/jquery-throttle-debounce/raw/master/jquery.ba-throttle-debounce.min.js (0.7kb)
// 
// About: License
// 
// Copyright (c) 2010 "Cowboy" Ben Alman,
// Dual licensed under the MIT and GPL licenses.
// http://benalman.com/about/license/
// 
// About: Examples
// 
// These working examples, complete with fully commented code, illustrate a few
// ways in which this plugin can be used.
// 
// Throttle - http://benalman.com/code/projects/jquery-throttle-debounce/examples/throttle/
// Debounce - http://benalman.com/code/projects/jquery-throttle-debounce/examples/debounce/
// 
// About: Support and Testing
// 
// Information about what version or versions of jQuery this plugin has been
// tested with, what browsers it has been tested in, and where the unit tests
// reside (so you can test it yourself).
// 
// jQuery Versions - none, 1.3.2, 1.4.2
// Browsers Tested - Internet Explorer 6-8, Firefox 2-3.6, Safari 3-4, Chrome 4-5, Opera 9.6-10.1.
// Unit Tests      - http://benalman.com/code/projects/jquery-throttle-debounce/unit/
// 
// About: Release History
// 
// 1.1 - (3/7/2010) Fixed a bug in <jQuery.throttle> where trailing callbacks
//       executed later than they should. Reworked a fair amount of internal
//       logic as well.
// 1.0 - (3/6/2010) Initial release as a stand-alone project. Migrated over
//       from jquery-misc repo v0.4 to jquery-throttle repo v1.0, added the
//       no_trailing throttle parameter and debounce functionality.
// 
// Topic: Note for non-jQuery users
// 
// jQuery isn't actually required for this plugin, because nothing internal
// uses any jQuery methods or properties. jQuery is just used as a namespace
// under which these methods can exist.
// 
// Since jQuery isn't actually required for this plugin, if jQuery doesn't exist
// when this plugin is loaded, the method described below will be created in
// the `Cowboy` namespace. Usage will be exactly the same, but instead of
// $.method() or jQuery.method(), you'll need to use Cowboy.method().

(function(window,undefined){
  '$:nomunge'; // Used by YUI compressor.
  
  // Since jQuery really isn't required for this plugin, use `jQuery` as the
  // namespace only if it already exists, otherwise use the `Cowboy` namespace,
  // creating it if necessary.
  var $ = epGlobal.jQuery || window.Cowboy || ( window.Cowboy = {} ),
    
    // Internal method reference.
    jq_throttle;
  
  // Method: jQuery.throttle
  // 
  // Throttle execution of a function. Especially useful for rate limiting
  // execution of handlers on events like resize and scroll. If you want to
  // rate-limit execution of a function to a single time, see the
  // <jQuery.debounce> method.
  // 
  // In this visualization, | is a throttled-function call and X is the actual
  // callback execution:
  // 
  // > Throttled with `no_trailing` specified as false or unspecified:
  // > ||||||||||||||||||||||||| (pause) |||||||||||||||||||||||||
  // > X    X    X    X    X    X        X    X    X    X    X    X
  // > 
  // > Throttled with `no_trailing` specified as true:
  // > ||||||||||||||||||||||||| (pause) |||||||||||||||||||||||||
  // > X    X    X    X    X             X    X    X    X    X
  // 
  // Usage:
  // 
  // > var throttled = jQuery.throttle( delay, [ no_trailing, ] callback );
  // > 
  // > jQuery('selector').bind( 'someevent', throttled );
  // > jQuery('selector').unbind( 'someevent', throttled );
  // 
  // This also works in jQuery 1.4+:
  // 
  // > jQuery('selector').bind( 'someevent', jQuery.throttle( delay, [ no_trailing, ] callback ) );
  // > jQuery('selector').unbind( 'someevent', callback );
  // 
  // Arguments:
  // 
  //  delay - (Number) A zero-or-greater delay in milliseconds. For event
  //    callbacks, values around 100 or 250 (or even higher) are most useful.
  //  no_trailing - (Boolean) Optional, defaults to false. If no_trailing is
  //    true, callback will only execute every `delay` milliseconds while the
  //    throttled-function is being called. If no_trailing is false or
  //    unspecified, callback will be executed one final time after the last
  //    throttled-function call. (After the throttled-function has not been
  //    called for `delay` milliseconds, the internal counter is reset)
  //  callback - (Function) A function to be executed after delay milliseconds.
  //    The `this` context and all arguments are passed through, as-is, to
  //    `callback` when the throttled-function is executed.
  // 
  // Returns:
  // 
  //  (Function) A new, throttled, function.
  
  $.throttle = jq_throttle = function( delay, no_trailing, callback, debounce_mode ) {
    // After wrapper has stopped being called, this timeout ensures that
    // `callback` is executed at the proper times in `throttle` and `end`
    // debounce modes.
    var timeout_id,
      
      // Keep track of the last time `callback` was executed.
      last_exec = 0;
    
    // `no_trailing` defaults to falsy.
    if ( typeof no_trailing !== 'boolean' ) {
      debounce_mode = callback;
      callback = no_trailing;
      no_trailing = undefined;
    }
    
    // The `wrapper` function encapsulates all of the throttling / debouncing
    // functionality and when executed will limit the rate at which `callback`
    // is executed.
    function wrapper() {
      var that = this,
        elapsed = +new Date() - last_exec,
        args = arguments;
      
      // Execute `callback` and update the `last_exec` timestamp.
      function exec() {
        last_exec = +new Date();
        callback.apply( that, args );
      };
      
      // If `debounce_mode` is true (at_begin) this is used to clear the flag
      // to allow future `callback` executions.
      function clear() {
        timeout_id = undefined;
      };
      
      if ( debounce_mode && !timeout_id ) {
        // Since `wrapper` is being called for the first time and
        // `debounce_mode` is true (at_begin), execute `callback`.
        exec();
      }
      
      // Clear any existing timeout.
      timeout_id && clearTimeout( timeout_id );
      
      if ( debounce_mode === undefined && elapsed > delay ) {
        // In throttle mode, if `delay` time has been exceeded, execute
        // `callback`.
        exec();
        
      } else if ( no_trailing !== true ) {
        // In trailing throttle mode, since `delay` time has not been
        // exceeded, schedule `callback` to execute `delay` ms after most
        // recent execution.
        // 
        // If `debounce_mode` is true (at_begin), schedule `clear` to execute
        // after `delay` ms.
        // 
        // If `debounce_mode` is false (at end), schedule `callback` to
        // execute after `delay` ms.
        timeout_id = setTimeout( debounce_mode ? clear : exec, debounce_mode === undefined ? delay - elapsed : delay );
      }
    };
    
    // Set the guid of `wrapper` function to the same of original callback, so
    // it can be removed in jQuery 1.4+ .unbind or .die by using the original
    // callback as a reference.
    if ( $.guid ) {
      wrapper.guid = callback.guid = callback.guid || $.guid++;
    }
    
    // Return the wrapper function.
    return wrapper;
  };
  
  // Method: jQuery.debounce
  // 
  // Debounce execution of a function. Debouncing, unlike throttling,
  // guarantees that a function is only executed a single time, either at the
  // very beginning of a series of calls, or at the very end. If you want to
  // simply rate-limit execution of a function, see the <jQuery.throttle>
  // method.
  // 
  // In this visualization, | is a debounced-function call and X is the actual
  // callback execution:
  // 
  // > Debounced with `at_begin` specified as false or unspecified:
  // > ||||||||||||||||||||||||| (pause) |||||||||||||||||||||||||
  // >                          X                                 X
  // > 
  // > Debounced with `at_begin` specified as true:
  // > ||||||||||||||||||||||||| (pause) |||||||||||||||||||||||||
  // > X                                 X
  // 
  // Usage:
  // 
  // > var debounced = jQuery.debounce( delay, [ at_begin, ] callback );
  // > 
  // > jQuery('selector').bind( 'someevent', debounced );
  // > jQuery('selector').unbind( 'someevent', debounced );
  // 
  // This also works in jQuery 1.4+:
  // 
  // > jQuery('selector').bind( 'someevent', jQuery.debounce( delay, [ at_begin, ] callback ) );
  // > jQuery('selector').unbind( 'someevent', callback );
  // 
  // Arguments:
  // 
  //  delay - (Number) A zero-or-greater delay in milliseconds. For event
  //    callbacks, values around 100 or 250 (or even higher) are most useful.
  //  at_begin - (Boolean) Optional, defaults to false. If at_begin is false or
  //    unspecified, callback will only be executed `delay` milliseconds after
  //    the last debounced-function call. If at_begin is true, callback will be
  //    executed only at the first debounced-function call. (After the
  //    throttled-function has not been called for `delay` milliseconds, the
  //    internal counter is reset)
  //  callback - (Function) A function to be executed after delay milliseconds.
  //    The `this` context and all arguments are passed through, as-is, to
  //    `callback` when the debounced-function is executed.
  // 
  // Returns:
  // 
  //  (Function) A new, debounced, function.
  
  $.debounce = function( delay, at_begin, callback ) {
    return callback === undefined
      ? jq_throttle( delay, at_begin, false )
      : jq_throttle( delay, callback, at_begin !== false );
  };
  
})(this);

(function(e){e.fn.hoverIntent=function(a,f,p){var b={interval:100,sensitivity:7,timeout:0},b="object"===typeof a?e.extend(b,a):e.isFunction(f)?e.extend(b,{over:a,out:f,selector:p}):e.extend(b,{over:a,out:a,selector:f}),g,h,k,l,m=function(b){g=b.pageX;h=b.pageY},n=function(a,d){d.hoverIntent_t=clearTimeout(d.hoverIntent_t);if(Math.abs(k-g)+Math.abs(l-h)<b.sensitivity)return e(d).off("mousemove.hoverIntent",m),d.hoverIntent_s=1,b.over.apply(d,[a]);k=g;l=h;d.hoverIntent_t=setTimeout(function(){n(a,d)},
b.interval)};a=function(a){var d=epGlobal.jQuery.extend({},a),c=this;c.hoverIntent_t&&(c.hoverIntent_t=clearTimeout(c.hoverIntent_t));"mouseenter"==a.type?(k=d.pageX,l=d.pageY,e(c).on("mousemove.hoverIntent",m),1!=c.hoverIntent_s&&(c.hoverIntent_t=setTimeout(function(){n(d,c)},b.interval))):(e(c).off("mousemove.hoverIntent",m),1==c.hoverIntent_s&&(c.hoverIntent_t=setTimeout(function(){c.hoverIntent_t=clearTimeout(c.hoverIntent_t);c.hoverIntent_s=0;b.out.apply(c,[d])},b.timeout)))};return this.on({"mouseenter.hoverIntent":a,
"mouseleave.hoverIntent":a},b.selector)}})(epGlobal.jQuery);

//---------------------------------------------------------------------
// QRCode for JavaScript
//
// Copyright (c) 2009 Kazuhiko Arase
//
// URL: http://www.d-project.com/
//
// Licensed under the MIT license:
//   http://www.opensource.org/licenses/mit-license.php
//
// The word "QR Code" is registered trademark of 
// DENSO WAVE INCORPORATED
//   http://www.denso-wave.com/qrcode/faqpatent-e.html
//
//---------------------------------------------------------------------

//---------------------------------------------------------------------
// QR8bitByte
//---------------------------------------------------------------------

function QR8bitByte(data) {
	this.mode = QRMode.MODE_8BIT_BYTE;
	this.data = data;
}

QR8bitByte.prototype = {

	getLength : function(buffer) {
		return this.data.length;
	},
	
	write : function(buffer) {
		for (var i = 0; i < this.data.length; i++) {
			// not JIS ...
			buffer.put(this.data.charCodeAt(i), 8);
		}
	}
};

//---------------------------------------------------------------------
// QRCode
//---------------------------------------------------------------------

function QRCode(typeNumber, errorCorrectLevel) {
	this.typeNumber = typeNumber;
	this.errorCorrectLevel = errorCorrectLevel;
	this.modules = null;
	this.moduleCount = 0;
	this.dataCache = null;
	this.dataList = new Array();
}

QRCode.prototype = {
	
	addData : function(data) {
		var newData = new QR8bitByte(data);
		this.dataList.push(newData);
		this.dataCache = null;
	},
	
	isDark : function(row, col) {
		if (row < 0 || this.moduleCount <= row || col < 0 || this.moduleCount <= col) {
			throw new Error(row + "," + col);
		}
		return this.modules[row][col];
	},

	getModuleCount : function() {
		return this.moduleCount;
	},
	
	make : function() {
		// Calculate automatically typeNumber if provided is < 1
		if (this.typeNumber < 1 ){
			var typeNumber = 1;
			for (typeNumber = 1; typeNumber < 40; typeNumber++) {
				var rsBlocks = QRRSBlock.getRSBlocks(typeNumber, this.errorCorrectLevel);

				var buffer = new QRBitBuffer();
				var totalDataCount = 0;
				for (var i = 0; i < rsBlocks.length; i++) {
					totalDataCount += rsBlocks[i].dataCount;
				}

				for (var i = 0; i < this.dataList.length; i++) {
					var data = this.dataList[i];
					buffer.put(data.mode, 4);
					buffer.put(data.getLength(), QRUtil.getLengthInBits(data.mode, typeNumber) );
					data.write(buffer);
				}
				if (buffer.getLengthInBits() <= totalDataCount * 8)
					break;
			}
			this.typeNumber = typeNumber;
		}
		this.makeImpl(false, this.getBestMaskPattern() );
	},
	
	makeImpl : function(test, maskPattern) {
		
		this.moduleCount = this.typeNumber * 4 + 17;
		this.modules = new Array(this.moduleCount);
		
		for (var row = 0; row < this.moduleCount; row++) {
			
			this.modules[row] = new Array(this.moduleCount);
			
			for (var col = 0; col < this.moduleCount; col++) {
				this.modules[row][col] = null;//(col + row) % 3;
			}
		}
	
		this.setupPositionProbePattern(0, 0);
		this.setupPositionProbePattern(this.moduleCount - 7, 0);
		this.setupPositionProbePattern(0, this.moduleCount - 7);
		this.setupPositionAdjustPattern();
		this.setupTimingPattern();
		this.setupTypeInfo(test, maskPattern);
		
		if (this.typeNumber >= 7) {
			this.setupTypeNumber(test);
		}
	
		if (this.dataCache == null) {
			this.dataCache = QRCode.createData(this.typeNumber, this.errorCorrectLevel, this.dataList);
		}
	
		this.mapData(this.dataCache, maskPattern);
	},

	setupPositionProbePattern : function(row, col)  {
		
		for (var r = -1; r <= 7; r++) {
			
			if (row + r <= -1 || this.moduleCount <= row + r) continue;
			
			for (var c = -1; c <= 7; c++) {
				
				if (col + c <= -1 || this.moduleCount <= col + c) continue;
				
				if ( (0 <= r && r <= 6 && (c == 0 || c == 6) )
						|| (0 <= c && c <= 6 && (r == 0 || r == 6) )
						|| (2 <= r && r <= 4 && 2 <= c && c <= 4) ) {
					this.modules[row + r][col + c] = true;
				} else {
					this.modules[row + r][col + c] = false;
				}
			}		
		}		
	},
	
	getBestMaskPattern : function() {
	
		var minLostPoint = 0;
		var pattern = 0;
	
		for (var i = 0; i < 8; i++) {
			
			this.makeImpl(true, i);
	
			var lostPoint = QRUtil.getLostPoint(this);
	
			if (i == 0 || minLostPoint >  lostPoint) {
				minLostPoint = lostPoint;
				pattern = i;
			}
		}
	
		return pattern;
	},
	
	createMovieClip : function(target_mc, instance_name, depth) {
	
		var qr_mc = target_mc.createEmptyMovieClip(instance_name, depth);
		var cs = 1;
	
		this.make();

		for (var row = 0; row < this.modules.length; row++) {
			
			var y = row * cs;
			
			for (var col = 0; col < this.modules[row].length; col++) {
	
				var x = col * cs;
				var dark = this.modules[row][col];
			
				if (dark) {
					qr_mc.beginFill(0, 100);
					qr_mc.moveTo(x, y);
					qr_mc.lineTo(x + cs, y);
					qr_mc.lineTo(x + cs, y + cs);
					qr_mc.lineTo(x, y + cs);
					qr_mc.endFill();
				}
			}
		}
		
		return qr_mc;
	},

	setupTimingPattern : function() {
		
		for (var r = 8; r < this.moduleCount - 8; r++) {
			if (this.modules[r][6] != null) {
				continue;
			}
			this.modules[r][6] = (r % 2 == 0);
		}
	
		for (var c = 8; c < this.moduleCount - 8; c++) {
			if (this.modules[6][c] != null) {
				continue;
			}
			this.modules[6][c] = (c % 2 == 0);
		}
	},
	
	setupPositionAdjustPattern : function() {
	
		var pos = QRUtil.getPatternPosition(this.typeNumber);
		
		for (var i = 0; i < pos.length; i++) {
		
			for (var j = 0; j < pos.length; j++) {
			
				var row = pos[i];
				var col = pos[j];
				
				if (this.modules[row][col] != null) {
					continue;
				}
				
				for (var r = -2; r <= 2; r++) {
				
					for (var c = -2; c <= 2; c++) {
					
						if (r == -2 || r == 2 || c == -2 || c == 2 
								|| (r == 0 && c == 0) ) {
							this.modules[row + r][col + c] = true;
						} else {
							this.modules[row + r][col + c] = false;
						}
					}
				}
			}
		}
	},
	
	setupTypeNumber : function(test) {
	
		var bits = QRUtil.getBCHTypeNumber(this.typeNumber);
	
		for (var i = 0; i < 18; i++) {
			var mod = (!test && ( (bits >> i) & 1) == 1);
			this.modules[Math.floor(i / 3)][i % 3 + this.moduleCount - 8 - 3] = mod;
		}
	
		for (var i = 0; i < 18; i++) {
			var mod = (!test && ( (bits >> i) & 1) == 1);
			this.modules[i % 3 + this.moduleCount - 8 - 3][Math.floor(i / 3)] = mod;
		}
	},
	
	setupTypeInfo : function(test, maskPattern) {
	
		var data = (this.errorCorrectLevel << 3) | maskPattern;
		var bits = QRUtil.getBCHTypeInfo(data);
	
		// vertical		
		for (var i = 0; i < 15; i++) {
	
			var mod = (!test && ( (bits >> i) & 1) == 1);
	
			if (i < 6) {
				this.modules[i][8] = mod;
			} else if (i < 8) {
				this.modules[i + 1][8] = mod;
			} else {
				this.modules[this.moduleCount - 15 + i][8] = mod;
			}
		}
	
		// horizontal
		for (var i = 0; i < 15; i++) {
	
			var mod = (!test && ( (bits >> i) & 1) == 1);
			
			if (i < 8) {
				this.modules[8][this.moduleCount - i - 1] = mod;
			} else if (i < 9) {
				this.modules[8][15 - i - 1 + 1] = mod;
			} else {
				this.modules[8][15 - i - 1] = mod;
			}
		}
	
		// fixed module
		this.modules[this.moduleCount - 8][8] = (!test);
	
	},
	
	mapData : function(data, maskPattern) {
		
		var inc = -1;
		var row = this.moduleCount - 1;
		var bitIndex = 7;
		var byteIndex = 0;
		
		for (var col = this.moduleCount - 1; col > 0; col -= 2) {
	
			if (col == 6) col--;
	
			while (true) {
	
				for (var c = 0; c < 2; c++) {
					
					if (this.modules[row][col - c] == null) {
						
						var dark = false;
	
						if (byteIndex < data.length) {
							dark = ( ( (data[byteIndex] >>> bitIndex) & 1) == 1);
						}
	
						var mask = QRUtil.getMask(maskPattern, row, col - c);
	
						if (mask) {
							dark = !dark;
						}
						
						this.modules[row][col - c] = dark;
						bitIndex--;
	
						if (bitIndex == -1) {
							byteIndex++;
							bitIndex = 7;
						}
					}
				}
								
				row += inc;
	
				if (row < 0 || this.moduleCount <= row) {
					row -= inc;
					inc = -inc;
					break;
				}
			}
		}
		
	}

};

QRCode.PAD0 = 0xEC;
QRCode.PAD1 = 0x11;

QRCode.createData = function(typeNumber, errorCorrectLevel, dataList) {
	
	var rsBlocks = QRRSBlock.getRSBlocks(typeNumber, errorCorrectLevel);
	
	var buffer = new QRBitBuffer();
	
	for (var i = 0; i < dataList.length; i++) {
		var data = dataList[i];
		buffer.put(data.mode, 4);
		buffer.put(data.getLength(), QRUtil.getLengthInBits(data.mode, typeNumber) );
		data.write(buffer);
	}

	// calc num max data.
	var totalDataCount = 0;
	for (var i = 0; i < rsBlocks.length; i++) {
		totalDataCount += rsBlocks[i].dataCount;
	}

	if (buffer.getLengthInBits() > totalDataCount * 8) {
		throw new Error("code length overflow. ("
			+ buffer.getLengthInBits()
			+ ">"
			+  totalDataCount * 8
			+ ")");
	}

	// end code
	if (buffer.getLengthInBits() + 4 <= totalDataCount * 8) {
		buffer.put(0, 4);
	}

	// padding
	while (buffer.getLengthInBits() % 8 != 0) {
		buffer.putBit(false);
	}

	// padding
	while (true) {
		
		if (buffer.getLengthInBits() >= totalDataCount * 8) {
			break;
		}
		buffer.put(QRCode.PAD0, 8);
		
		if (buffer.getLengthInBits() >= totalDataCount * 8) {
			break;
		}
		buffer.put(QRCode.PAD1, 8);
	}

	return QRCode.createBytes(buffer, rsBlocks);
}

QRCode.createBytes = function(buffer, rsBlocks) {

	var offset = 0;
	
	var maxDcCount = 0;
	var maxEcCount = 0;
	
	var dcdata = new Array(rsBlocks.length);
	var ecdata = new Array(rsBlocks.length);
	
	for (var r = 0; r < rsBlocks.length; r++) {

		var dcCount = rsBlocks[r].dataCount;
		var ecCount = rsBlocks[r].totalCount - dcCount;

		maxDcCount = Math.max(maxDcCount, dcCount);
		maxEcCount = Math.max(maxEcCount, ecCount);
		
		dcdata[r] = new Array(dcCount);
		
		for (var i = 0; i < dcdata[r].length; i++) {
			dcdata[r][i] = 0xff & buffer.buffer[i + offset];
		}
		offset += dcCount;
		
		var rsPoly = QRUtil.getErrorCorrectPolynomial(ecCount);
		var rawPoly = new QRPolynomial(dcdata[r], rsPoly.getLength() - 1);

		var modPoly = rawPoly.mod(rsPoly);
		ecdata[r] = new Array(rsPoly.getLength() - 1);
		for (var i = 0; i < ecdata[r].length; i++) {
            var modIndex = i + modPoly.getLength() - ecdata[r].length;
			ecdata[r][i] = (modIndex >= 0)? modPoly.get(modIndex) : 0;
		}

	}
	
	var totalCodeCount = 0;
	for (var i = 0; i < rsBlocks.length; i++) {
		totalCodeCount += rsBlocks[i].totalCount;
	}

	var data = new Array(totalCodeCount);
	var index = 0;

	for (var i = 0; i < maxDcCount; i++) {
		for (var r = 0; r < rsBlocks.length; r++) {
			if (i < dcdata[r].length) {
				data[index++] = dcdata[r][i];
			}
		}
	}

	for (var i = 0; i < maxEcCount; i++) {
		for (var r = 0; r < rsBlocks.length; r++) {
			if (i < ecdata[r].length) {
				data[index++] = ecdata[r][i];
			}
		}
	}

	return data;

}

//---------------------------------------------------------------------
// QRMode
//---------------------------------------------------------------------

var QRMode = {
	MODE_NUMBER :		1 << 0,
	MODE_ALPHA_NUM : 	1 << 1,
	MODE_8BIT_BYTE : 	1 << 2,
	MODE_KANJI :		1 << 3
};

//---------------------------------------------------------------------
// QRErrorCorrectLevel
//---------------------------------------------------------------------
 
var QRErrorCorrectLevel = {
	L : 1,
	M : 0,
	Q : 3,
	H : 2
};

//---------------------------------------------------------------------
// QRMaskPattern
//---------------------------------------------------------------------

var QRMaskPattern = {
	PATTERN000 : 0,
	PATTERN001 : 1,
	PATTERN010 : 2,
	PATTERN011 : 3,
	PATTERN100 : 4,
	PATTERN101 : 5,
	PATTERN110 : 6,
	PATTERN111 : 7
};

//---------------------------------------------------------------------
// QRUtil
//---------------------------------------------------------------------
 
var QRUtil = {

    PATTERN_POSITION_TABLE : [
	    [],
	    [6, 18],
	    [6, 22],
	    [6, 26],
	    [6, 30],
	    [6, 34],
	    [6, 22, 38],
	    [6, 24, 42],
	    [6, 26, 46],
	    [6, 28, 50],
	    [6, 30, 54],		
	    [6, 32, 58],
	    [6, 34, 62],
	    [6, 26, 46, 66],
	    [6, 26, 48, 70],
	    [6, 26, 50, 74],
	    [6, 30, 54, 78],
	    [6, 30, 56, 82],
	    [6, 30, 58, 86],
	    [6, 34, 62, 90],
	    [6, 28, 50, 72, 94],
	    [6, 26, 50, 74, 98],
	    [6, 30, 54, 78, 102],
	    [6, 28, 54, 80, 106],
	    [6, 32, 58, 84, 110],
	    [6, 30, 58, 86, 114],
	    [6, 34, 62, 90, 118],
	    [6, 26, 50, 74, 98, 122],
	    [6, 30, 54, 78, 102, 126],
	    [6, 26, 52, 78, 104, 130],
	    [6, 30, 56, 82, 108, 134],
	    [6, 34, 60, 86, 112, 138],
	    [6, 30, 58, 86, 114, 142],
	    [6, 34, 62, 90, 118, 146],
	    [6, 30, 54, 78, 102, 126, 150],
	    [6, 24, 50, 76, 102, 128, 154],
	    [6, 28, 54, 80, 106, 132, 158],
	    [6, 32, 58, 84, 110, 136, 162],
	    [6, 26, 54, 82, 110, 138, 166],
	    [6, 30, 58, 86, 114, 142, 170]
    ],

    G15 : (1 << 10) | (1 << 8) | (1 << 5) | (1 << 4) | (1 << 2) | (1 << 1) | (1 << 0),
    G18 : (1 << 12) | (1 << 11) | (1 << 10) | (1 << 9) | (1 << 8) | (1 << 5) | (1 << 2) | (1 << 0),
    G15_MASK : (1 << 14) | (1 << 12) | (1 << 10)	| (1 << 4) | (1 << 1),

    getBCHTypeInfo : function(data) {
	    var d = data << 10;
	    while (QRUtil.getBCHDigit(d) - QRUtil.getBCHDigit(QRUtil.G15) >= 0) {
		    d ^= (QRUtil.G15 << (QRUtil.getBCHDigit(d) - QRUtil.getBCHDigit(QRUtil.G15) ) ); 	
	    }
	    return ( (data << 10) | d) ^ QRUtil.G15_MASK;
    },

    getBCHTypeNumber : function(data) {
	    var d = data << 12;
	    while (QRUtil.getBCHDigit(d) - QRUtil.getBCHDigit(QRUtil.G18) >= 0) {
		    d ^= (QRUtil.G18 << (QRUtil.getBCHDigit(d) - QRUtil.getBCHDigit(QRUtil.G18) ) ); 	
	    }
	    return (data << 12) | d;
    },

    getBCHDigit : function(data) {

	    var digit = 0;

	    while (data != 0) {
		    digit++;
		    data >>>= 1;
	    }

	    return digit;
    },

    getPatternPosition : function(typeNumber) {
	    return QRUtil.PATTERN_POSITION_TABLE[typeNumber - 1];
    },

    getMask : function(maskPattern, i, j) {
	    
	    switch (maskPattern) {
		    
	    case QRMaskPattern.PATTERN000 : return (i + j) % 2 == 0;
	    case QRMaskPattern.PATTERN001 : return i % 2 == 0;
	    case QRMaskPattern.PATTERN010 : return j % 3 == 0;
	    case QRMaskPattern.PATTERN011 : return (i + j) % 3 == 0;
	    case QRMaskPattern.PATTERN100 : return (Math.floor(i / 2) + Math.floor(j / 3) ) % 2 == 0;
	    case QRMaskPattern.PATTERN101 : return (i * j) % 2 + (i * j) % 3 == 0;
	    case QRMaskPattern.PATTERN110 : return ( (i * j) % 2 + (i * j) % 3) % 2 == 0;
	    case QRMaskPattern.PATTERN111 : return ( (i * j) % 3 + (i + j) % 2) % 2 == 0;

	    default :
		    throw new Error("bad maskPattern:" + maskPattern);
	    }
    },

    getErrorCorrectPolynomial : function(errorCorrectLength) {

	    var a = new QRPolynomial([1], 0);

	    for (var i = 0; i < errorCorrectLength; i++) {
		    a = a.multiply(new QRPolynomial([1, QRMath.gexp(i)], 0) );
	    }

	    return a;
    },

    getLengthInBits : function(mode, type) {

	    if (1 <= type && type < 10) {

		    // 1 - 9

		    switch(mode) {
		    case QRMode.MODE_NUMBER 	: return 10;
		    case QRMode.MODE_ALPHA_NUM 	: return 9;
		    case QRMode.MODE_8BIT_BYTE	: return 8;
		    case QRMode.MODE_KANJI  	: return 8;
		    default :
			    throw new Error("mode:" + mode);
		    }

	    } else if (type < 27) {

		    // 10 - 26

		    switch(mode) {
		    case QRMode.MODE_NUMBER 	: return 12;
		    case QRMode.MODE_ALPHA_NUM 	: return 11;
		    case QRMode.MODE_8BIT_BYTE	: return 16;
		    case QRMode.MODE_KANJI  	: return 10;
		    default :
			    throw new Error("mode:" + mode);
		    }

	    } else if (type < 41) {

		    // 27 - 40

		    switch(mode) {
		    case QRMode.MODE_NUMBER 	: return 14;
		    case QRMode.MODE_ALPHA_NUM	: return 13;
		    case QRMode.MODE_8BIT_BYTE	: return 16;
		    case QRMode.MODE_KANJI  	: return 12;
		    default :
			    throw new Error("mode:" + mode);
		    }

	    } else {
		    throw new Error("type:" + type);
	    }
    },

    getLostPoint : function(qrCode) {
	    
	    var moduleCount = qrCode.getModuleCount();
	    
	    var lostPoint = 0;
	    
	    // LEVEL1
	    
	    for (var row = 0; row < moduleCount; row++) {

		    for (var col = 0; col < moduleCount; col++) {

			    var sameCount = 0;
			    var dark = qrCode.isDark(row, col);

				for (var r = -1; r <= 1; r++) {

				    if (row + r < 0 || moduleCount <= row + r) {
					    continue;
				    }

				    for (var c = -1; c <= 1; c++) {

					    if (col + c < 0 || moduleCount <= col + c) {
						    continue;
					    }

					    if (r == 0 && c == 0) {
						    continue;
					    }

					    if (dark == qrCode.isDark(row + r, col + c) ) {
						    sameCount++;
					    }
				    }
			    }

			    if (sameCount > 5) {
				    lostPoint += (3 + sameCount - 5);
			    }
		    }
	    }

	    // LEVEL2

	    for (var row = 0; row < moduleCount - 1; row++) {
		    for (var col = 0; col < moduleCount - 1; col++) {
			    var count = 0;
			    if (qrCode.isDark(row,     col    ) ) count++;
			    if (qrCode.isDark(row + 1, col    ) ) count++;
			    if (qrCode.isDark(row,     col + 1) ) count++;
			    if (qrCode.isDark(row + 1, col + 1) ) count++;
			    if (count == 0 || count == 4) {
				    lostPoint += 3;
			    }
		    }
	    }

	    // LEVEL3

	    for (var row = 0; row < moduleCount; row++) {
		    for (var col = 0; col < moduleCount - 6; col++) {
			    if (qrCode.isDark(row, col)
					    && !qrCode.isDark(row, col + 1)
					    &&  qrCode.isDark(row, col + 2)
					    &&  qrCode.isDark(row, col + 3)
					    &&  qrCode.isDark(row, col + 4)
					    && !qrCode.isDark(row, col + 5)
					    &&  qrCode.isDark(row, col + 6) ) {
				    lostPoint += 40;
			    }
		    }
	    }

	    for (var col = 0; col < moduleCount; col++) {
		    for (var row = 0; row < moduleCount - 6; row++) {
			    if (qrCode.isDark(row, col)
					    && !qrCode.isDark(row + 1, col)
					    &&  qrCode.isDark(row + 2, col)
					    &&  qrCode.isDark(row + 3, col)
					    &&  qrCode.isDark(row + 4, col)
					    && !qrCode.isDark(row + 5, col)
					    &&  qrCode.isDark(row + 6, col) ) {
				    lostPoint += 40;
			    }
		    }
	    }

	    // LEVEL4
	    
	    var darkCount = 0;

	    for (var col = 0; col < moduleCount; col++) {
		    for (var row = 0; row < moduleCount; row++) {
			    if (qrCode.isDark(row, col) ) {
				    darkCount++;
			    }
		    }
	    }
	    
	    var ratio = Math.abs(100 * darkCount / moduleCount / moduleCount - 50) / 5;
	    lostPoint += ratio * 10;

	    return lostPoint;		
    }

};


//---------------------------------------------------------------------
// QRMath
//---------------------------------------------------------------------

var QRMath = {

	glog : function(n) {
	
		if (n < 1) {
			throw new Error("glog(" + n + ")");
		}
		
		return QRMath.LOG_TABLE[n];
	},
	
	gexp : function(n) {
	
		while (n < 0) {
			n += 255;
		}
	
		while (n >= 256) {
			n -= 255;
		}
	
		return QRMath.EXP_TABLE[n];
	},
	
	EXP_TABLE : new Array(256),
	
	LOG_TABLE : new Array(256)

};
	
for (var i = 0; i < 8; i++) {
	QRMath.EXP_TABLE[i] = 1 << i;
}
for (var i = 8; i < 256; i++) {
	QRMath.EXP_TABLE[i] = QRMath.EXP_TABLE[i - 4]
		^ QRMath.EXP_TABLE[i - 5]
		^ QRMath.EXP_TABLE[i - 6]
		^ QRMath.EXP_TABLE[i - 8];
}
for (var i = 0; i < 255; i++) {
	QRMath.LOG_TABLE[QRMath.EXP_TABLE[i] ] = i;
}

//---------------------------------------------------------------------
// QRPolynomial
//---------------------------------------------------------------------

function QRPolynomial(num, shift) {

	if (num.length == undefined) {
		throw new Error(num.length + "/" + shift);
	}

	var offset = 0;

	while (offset < num.length && num[offset] == 0) {
		offset++;
	}

	this.num = new Array(num.length - offset + shift);
	for (var i = 0; i < num.length - offset; i++) {
		this.num[i] = num[i + offset];
	}
}

QRPolynomial.prototype = {

	get : function(index) {
		return this.num[index];
	},
	
	getLength : function() {
		return this.num.length;
	},
	
	multiply : function(e) {
	
		var num = new Array(this.getLength() + e.getLength() - 1);
	
		for (var i = 0; i < this.getLength(); i++) {
			for (var j = 0; j < e.getLength(); j++) {
				num[i + j] ^= QRMath.gexp(QRMath.glog(this.get(i) ) + QRMath.glog(e.get(j) ) );
			}
		}
	
		return new QRPolynomial(num, 0);
	},
	
	mod : function(e) {
	
		if (this.getLength() - e.getLength() < 0) {
			return this;
		}
	
		var ratio = QRMath.glog(this.get(0) ) - QRMath.glog(e.get(0) );
	
		var num = new Array(this.getLength() );
		
		for (var i = 0; i < this.getLength(); i++) {
			num[i] = this.get(i);
		}
		
		for (var i = 0; i < e.getLength(); i++) {
			num[i] ^= QRMath.gexp(QRMath.glog(e.get(i) ) + ratio);
		}
	
		// recursive call
		return new QRPolynomial(num, 0).mod(e);
	}
};

//---------------------------------------------------------------------
// QRRSBlock
//---------------------------------------------------------------------

function QRRSBlock(totalCount, dataCount) {
	this.totalCount = totalCount;
	this.dataCount  = dataCount;
}

QRRSBlock.RS_BLOCK_TABLE = [

	// L
	// M
	// Q
	// H

	// 1
	[1, 26, 19],
	[1, 26, 16],
	[1, 26, 13],
	[1, 26, 9],
	
	// 2
	[1, 44, 34],
	[1, 44, 28],
	[1, 44, 22],
	[1, 44, 16],

	// 3
	[1, 70, 55],
	[1, 70, 44],
	[2, 35, 17],
	[2, 35, 13],

	// 4		
	[1, 100, 80],
	[2, 50, 32],
	[2, 50, 24],
	[4, 25, 9],
	
	// 5
	[1, 134, 108],
	[2, 67, 43],
	[2, 33, 15, 2, 34, 16],
	[2, 33, 11, 2, 34, 12],
	
	// 6
	[2, 86, 68],
	[4, 43, 27],
	[4, 43, 19],
	[4, 43, 15],
	
	// 7		
	[2, 98, 78],
	[4, 49, 31],
	[2, 32, 14, 4, 33, 15],
	[4, 39, 13, 1, 40, 14],
	
	// 8
	[2, 121, 97],
	[2, 60, 38, 2, 61, 39],
	[4, 40, 18, 2, 41, 19],
	[4, 40, 14, 2, 41, 15],
	
	// 9
	[2, 146, 116],
	[3, 58, 36, 2, 59, 37],
	[4, 36, 16, 4, 37, 17],
	[4, 36, 12, 4, 37, 13],
	
	// 10		
	[2, 86, 68, 2, 87, 69],
	[4, 69, 43, 1, 70, 44],
	[6, 43, 19, 2, 44, 20],
	[6, 43, 15, 2, 44, 16],

	// 11
	[4, 101, 81],
	[1, 80, 50, 4, 81, 51],
	[4, 50, 22, 4, 51, 23],
	[3, 36, 12, 8, 37, 13],

	// 12
	[2, 116, 92, 2, 117, 93],
	[6, 58, 36, 2, 59, 37],
	[4, 46, 20, 6, 47, 21],
	[7, 42, 14, 4, 43, 15],

	// 13
	[4, 133, 107],
	[8, 59, 37, 1, 60, 38],
	[8, 44, 20, 4, 45, 21],
	[12, 33, 11, 4, 34, 12],

	// 14
	[3, 145, 115, 1, 146, 116],
	[4, 64, 40, 5, 65, 41],
	[11, 36, 16, 5, 37, 17],
	[11, 36, 12, 5, 37, 13],

	// 15
	[5, 109, 87, 1, 110, 88],
	[5, 65, 41, 5, 66, 42],
	[5, 54, 24, 7, 55, 25],
	[11, 36, 12],

	// 16
	[5, 122, 98, 1, 123, 99],
	[7, 73, 45, 3, 74, 46],
	[15, 43, 19, 2, 44, 20],
	[3, 45, 15, 13, 46, 16],

	// 17
	[1, 135, 107, 5, 136, 108],
	[10, 74, 46, 1, 75, 47],
	[1, 50, 22, 15, 51, 23],
	[2, 42, 14, 17, 43, 15],

	// 18
	[5, 150, 120, 1, 151, 121],
	[9, 69, 43, 4, 70, 44],
	[17, 50, 22, 1, 51, 23],
	[2, 42, 14, 19, 43, 15],

	// 19
	[3, 141, 113, 4, 142, 114],
	[3, 70, 44, 11, 71, 45],
	[17, 47, 21, 4, 48, 22],
	[9, 39, 13, 16, 40, 14],

	// 20
	[3, 135, 107, 5, 136, 108],
	[3, 67, 41, 13, 68, 42],
	[15, 54, 24, 5, 55, 25],
	[15, 43, 15, 10, 44, 16],

	// 21
	[4, 144, 116, 4, 145, 117],
	[17, 68, 42],
	[17, 50, 22, 6, 51, 23],
	[19, 46, 16, 6, 47, 17],

	// 22
	[2, 139, 111, 7, 140, 112],
	[17, 74, 46],
	[7, 54, 24, 16, 55, 25],
	[34, 37, 13],

	// 23
	[4, 151, 121, 5, 152, 122],
	[4, 75, 47, 14, 76, 48],
	[11, 54, 24, 14, 55, 25],
	[16, 45, 15, 14, 46, 16],

	// 24
	[6, 147, 117, 4, 148, 118],
	[6, 73, 45, 14, 74, 46],
	[11, 54, 24, 16, 55, 25],
	[30, 46, 16, 2, 47, 17],

	// 25
	[8, 132, 106, 4, 133, 107],
	[8, 75, 47, 13, 76, 48],
	[7, 54, 24, 22, 55, 25],
	[22, 45, 15, 13, 46, 16],

	// 26
	[10, 142, 114, 2, 143, 115],
	[19, 74, 46, 4, 75, 47],
	[28, 50, 22, 6, 51, 23],
	[33, 46, 16, 4, 47, 17],

	// 27
	[8, 152, 122, 4, 153, 123],
	[22, 73, 45, 3, 74, 46],
	[8, 53, 23, 26, 54, 24],
	[12, 45, 15, 28, 46, 16],

	// 28
	[3, 147, 117, 10, 148, 118],
	[3, 73, 45, 23, 74, 46],
	[4, 54, 24, 31, 55, 25],
	[11, 45, 15, 31, 46, 16],

	// 29
	[7, 146, 116, 7, 147, 117],
	[21, 73, 45, 7, 74, 46],
	[1, 53, 23, 37, 54, 24],
	[19, 45, 15, 26, 46, 16],

	// 30
	[5, 145, 115, 10, 146, 116],
	[19, 75, 47, 10, 76, 48],
	[15, 54, 24, 25, 55, 25],
	[23, 45, 15, 25, 46, 16],

	// 31
	[13, 145, 115, 3, 146, 116],
	[2, 74, 46, 29, 75, 47],
	[42, 54, 24, 1, 55, 25],
	[23, 45, 15, 28, 46, 16],

	// 32
	[17, 145, 115],
	[10, 74, 46, 23, 75, 47],
	[10, 54, 24, 35, 55, 25],
	[19, 45, 15, 35, 46, 16],

	// 33
	[17, 145, 115, 1, 146, 116],
	[14, 74, 46, 21, 75, 47],
	[29, 54, 24, 19, 55, 25],
	[11, 45, 15, 46, 46, 16],

	// 34
	[13, 145, 115, 6, 146, 116],
	[14, 74, 46, 23, 75, 47],
	[44, 54, 24, 7, 55, 25],
	[59, 46, 16, 1, 47, 17],

	// 35
	[12, 151, 121, 7, 152, 122],
	[12, 75, 47, 26, 76, 48],
	[39, 54, 24, 14, 55, 25],
	[22, 45, 15, 41, 46, 16],

	// 36
	[6, 151, 121, 14, 152, 122],
	[6, 75, 47, 34, 76, 48],
	[46, 54, 24, 10, 55, 25],
	[2, 45, 15, 64, 46, 16],

	// 37
	[17, 152, 122, 4, 153, 123],
	[29, 74, 46, 14, 75, 47],
	[49, 54, 24, 10, 55, 25],
	[24, 45, 15, 46, 46, 16],

	// 38
	[4, 152, 122, 18, 153, 123],
	[13, 74, 46, 32, 75, 47],
	[48, 54, 24, 14, 55, 25],
	[42, 45, 15, 32, 46, 16],

	// 39
	[20, 147, 117, 4, 148, 118],
	[40, 75, 47, 7, 76, 48],
	[43, 54, 24, 22, 55, 25],
	[10, 45, 15, 67, 46, 16],

	// 40
	[19, 148, 118, 6, 149, 119],
	[18, 75, 47, 31, 76, 48],
	[34, 54, 24, 34, 55, 25],
	[20, 45, 15, 61, 46, 16]
];

QRRSBlock.getRSBlocks = function(typeNumber, errorCorrectLevel) {
	
	var rsBlock = QRRSBlock.getRsBlockTable(typeNumber, errorCorrectLevel);
	
	if (rsBlock == undefined) {
		throw new Error("bad rs block @ typeNumber:" + typeNumber + "/errorCorrectLevel:" + errorCorrectLevel);
	}

	var length = rsBlock.length / 3;
	
	var list = new Array();
	
	for (var i = 0; i < length; i++) {

		var count = rsBlock[i * 3 + 0];
		var totalCount = rsBlock[i * 3 + 1];
		var dataCount  = rsBlock[i * 3 + 2];

		for (var j = 0; j < count; j++) {
			list.push(new QRRSBlock(totalCount, dataCount) );	
		}
	}
	
	return list;
}

QRRSBlock.getRsBlockTable = function(typeNumber, errorCorrectLevel) {

	switch(errorCorrectLevel) {
	case QRErrorCorrectLevel.L :
		return QRRSBlock.RS_BLOCK_TABLE[(typeNumber - 1) * 4 + 0];
	case QRErrorCorrectLevel.M :
		return QRRSBlock.RS_BLOCK_TABLE[(typeNumber - 1) * 4 + 1];
	case QRErrorCorrectLevel.Q :
		return QRRSBlock.RS_BLOCK_TABLE[(typeNumber - 1) * 4 + 2];
	case QRErrorCorrectLevel.H :
		return QRRSBlock.RS_BLOCK_TABLE[(typeNumber - 1) * 4 + 3];
	default :
		return undefined;
	}
}

//---------------------------------------------------------------------
// QRBitBuffer
//---------------------------------------------------------------------

function QRBitBuffer() {
	this.buffer = new Array();
	this.length = 0;
}

QRBitBuffer.prototype = {

	get : function(index) {
		var bufIndex = Math.floor(index / 8);
		return ( (this.buffer[bufIndex] >>> (7 - index % 8) ) & 1) == 1;
	},
	
	put : function(num, length) {
		for (var i = 0; i < length; i++) {
			this.putBit( ( (num >>> (length - i - 1) ) & 1) == 1);
		}
	},
	
	getLengthInBits : function() {
		return this.length;
	},
	
	putBit : function(bit) {
	
		var bufIndex = Math.floor(this.length / 8);
		if (this.buffer.length <= bufIndex) {
			this.buffer.push(0);
		}
	
		if (bit) {
			this.buffer[bufIndex] |= (0x80 >>> (this.length % 8) );
		}
	
		this.length++;
	}
};

(function( $ ){
	$.fn.qrcode = function(options) {
		// if options is string, 
		if( typeof options === 'string' ){
			options	= { text: options };
		}

		// set default values
		// typeNumber < 1 for automatic calculation
		options	= $.extend( {}, {
			render		: "canvas",
			width		: 256,
			height		: 256,
			typeNumber	: -1,
			correctLevel	: QRErrorCorrectLevel.H,
                        background      : "#ffffff",
                        foreground      : "#000000"
		}, options);

		var createCanvas	= function(){
			// create the qrcode itself
			var qrcode	= new QRCode(options.typeNumber, options.correctLevel);
			qrcode.addData(options.text);
			qrcode.make();

			// create canvas element
			var canvas	= document.createElement('canvas');
			canvas.width	= options.width;
			canvas.height	= options.height;
			var ctx		= canvas.getContext('2d');

			// compute tileW/tileH based on options.width/options.height
			var tileW	= options.width  / qrcode.getModuleCount();
			var tileH	= options.height / qrcode.getModuleCount();

			// draw in the canvas
			for( var row = 0; row < qrcode.getModuleCount(); row++ ){
				for( var col = 0; col < qrcode.getModuleCount(); col++ ){
					ctx.fillStyle = qrcode.isDark(row, col) ? options.foreground : options.background;
					var w = (Math.ceil((col+1)*tileW) - Math.floor(col*tileW));
					var h = (Math.ceil((row+1)*tileW) - Math.floor(row*tileW));
					ctx.fillRect(Math.round(col*tileW),Math.round(row*tileH), w, h);  
				}	
			}
			// return just built canvas
			return canvas;
		}

		// from Jon-Carlos Rivera (https://github.com/imbcmdth)
		var createTable	= function(){
			// create the qrcode itself
			var qrcode	= new QRCode(options.typeNumber, options.correctLevel);
			qrcode.addData(options.text);
			qrcode.make();
			
			// create table element
			var $table	= $('<table></table>')
				.css("width", options.width+"px")
				.css("height", options.height+"px")
				.css("border", "0px")
				.css("border-collapse", "collapse")
				.css('background-color', options.background);
		  
			// compute tileS percentage
			var tileW	= options.width / qrcode.getModuleCount();
			var tileH	= options.height / qrcode.getModuleCount();

			// draw in the table
			for(var row = 0; row < qrcode.getModuleCount(); row++ ){
				var $row = $('<tr></tr>').css('height', tileH+"px").appendTo($table);
				
				for(var col = 0; col < qrcode.getModuleCount(); col++ ){
					$('<td></td>')
						.css('width', tileW+"px")
						.css('background-color', qrcode.isDark(row, col) ? options.foreground : options.background)
						.appendTo($row);
				}	
			}
			// return just built canvas
			return $table;
		}
  

		return this.each(function(){
			var element	= options.render == "canvas" ? createCanvas() : createTable();
			$(element).appendTo(this);
		});
	};
})( epGlobal.jQuery );

epGlobal.common = epGlobal.common || {};

epGlobal.common.engines = (function ($) {
    'use strict';

    function EngineInterface(source, destination) {
        this.init(source, destination);
    }

    $.extend(EngineInterface.prototype, {
        init: function (source, destination) {
            this.source = source;
            this.destination = destination;
            this.fsMode = false;
            var image = this.destination.find('.image-container');
            if (image.length == 1) {
                this.splashScreen = image.clone();
            }
        },

        load: function () {
        },

        dispose: function () {
        },

        setFullScreenMode: function () {
            this.fsMode = true;
        },

        _initPlayScreen: function (dimension) {
            var _this = this;
            if (this._playScreenClicked) {
                this.destination.empty();
                return false;
            } else {
                this._recreateSplashScreen();
                var a = this._playItem(dimension);
                a.click(function () {
                    _this._playScreenClicked = true;
                    _this.load();
                    return false;
                });
                this.destination.append(a);
                return true;
            }
        },

        _playItem: function (dimension) {
            var image, width, height;

            if (this.splashScreen) {
                image = new epGlobal.reader.womi.WOMIImageContainer(this.destination.find('.image-container'));
                image.load();
                width = height = '100%';
            } else {
                width = dimension.width + 'px';
                height = dimension.height + 'px';
            }

            var div = $('<div/>', {
                class: 'play-div',
                css: {
                    width: width,
                    height: height,
                    position: 'relative'
                }
            });

            var button = $('<button/>', {
                class: 'play-button',
                html: '<i class="icon-play-circle"></i>',
                css: {
                    position: 'absolute',
                    top: (dimension.height / 2 - 75) + 'px',
                    left: (dimension.width / 2 - 50) + 'px'
                }
            });

            div.append(button);

            if (image) {
                div.append(image._imgElement);
            }

            return div;
        },

        _playLabel: function () {
            return '';
        },

        _loadEngineScript: function (name, url, callback) {
            if ($('script[data-engine-name="' + name + '"]').length == 0) {
                epGlobal.head.js(url, function () {
                    callback();
                });
            } else {
                callback();
            }
        },

        _loadScripts: function (args) {
            epGlobal.head.js.apply(this, arguments);
        },

        _recreateSplashScreen: function () {
            if (this.splashScreen) {
                if (this.destination.find('.image-container').length != 1) {
                    this.destination.append(this.splashScreen.clone());
                }
            }
        }
    });

    function GeogebraEngine(source, destination) {
        this.init(source, destination);
    }

    $.extend(GeogebraEngine.prototype, EngineInterface.prototype, {
        scriptSrc: "//www.geogebra.org/web/4.2/web/web.nocache.js",
        maxPercentageHeight: 0.90,
        debounceTimeout: 500,

        load: function () {
            var dimensions = this.resizeArticleToContainer(this.destination);

            if (this._initPlayScreen(dimensions)) {
                $(window).on('resize', this.debouncedResizeHandler());
                return;
            } else {
                $(window).off('resize', this.debouncedResizeHandler());
            }
            var article = $(this.source);

            var script = document.createElement('script');
            script.src = this.scriptSrc;

            this.createIframe(this.destination, dimensions, function (iframe) {
                iframe.contents().find('body').append(article.clone());
                iframe[0].contentWindow.document.body.appendChild(script);
            });

            $(window).on('resize', this.debouncedResizeHandler());
        },

        dispose: function () {
            $(this.destination).children().remove();
            this._playScreenClicked = false;
            $(window).off('resize', this.debouncedResizeHandler());
        },

        createIframe: function (container, dimensions, onloadCallback, preLoadCallback) {
            var iframe = $('<iframe frameborder="0">').css({
                margin: 0,
                padding: 0,
                border: 'none',
                width: dimensions.width,
                height: dimensions.height
            });

            if (preLoadCallback) {
                preLoadCallback(iframe);
            }

            iframe.load(function () {
                iframe.contents().find('body').css({
                    margin: 0,
                    padding: 0
                });

                onloadCallback(iframe);
            });

            $(container).append(iframe);
        },

        resizeArticleToContainer: function (container) {
            var article = $(this.source);
            var ratio = $(container).width() / article.attr('data-param-width');

            var desiredWidth = $(container).width();
            var desiredHeight = article.attr('data-param-height') * ratio;

            var maxHeight = this.maxPercentageHeight * $(window).height();

            if (desiredHeight > maxHeight) {
                var scale = maxHeight / desiredHeight;
                desiredWidth *= scale;
                desiredHeight *= scale;
            }

            desiredWidth = Math.floor(desiredWidth);
            desiredHeight = Math.floor(desiredHeight);

            // XXX Geogebra seems to use some additional pixels to draw its border so account for that
            article.attr('data-param-width', desiredWidth - 3);
            article.attr('data-param-height', desiredHeight - 3);

            return {
                width: desiredWidth,
                height: desiredHeight
            };
        },

        debouncedResizeHandler: function () {
            var _this = this;

            if (this._debounceHandler) {
                return this._debounceHandler;
            } else {
                this._debounceHandler = $.debounce(_this.debounceTimeout, (function () {
                    var lastWidth = $(window).width();
                    var lastHeight = $(window).height();

                    return function () {
                        // This is a fix for behaviour seen on iPhone where address bar show/hide events cause resize events
                        if (lastHeight == $(window).height() && lastWidth == $(window).width()) {
                            return;
                        }

                        lastHeight = $(window).height();
                        lastWidth = $(window).width();

                        _this.dispose();
                        _this.load();
                    };
                }()));
                return this._debounceHandler;
            }
        }
    });

    function SwiffyEngine(source, destination) {
        this.init(source, destination);
    }

    $.extend(SwiffyEngine.prototype, GeogebraEngine.prototype, {
        scriptSrc: "//www.geogebra.org/web/4.2/web/web.nocache.js",

        load: function () {
            var srcUrl = this.source;
            var scriptSrc = this.scriptSrc;
            var dimensions = {
                width: $(this.destination).width(),
                height: $(this.destination).width() * $(this.destination).parent().data('height-ratio')
            };

            if (this._initPlayScreen(dimensions)) {
                $(window).on('resize', this.debouncedResizeHandler());
                return;
            } else {
                $(window).off('resize', this.debouncedResizeHandler());
            }

            this.createIframe(this.destination, dimensions, function (iframe) {
                var script = iframe[0].contentWindow.document.createElement('script');
                script.type = "text/javascript";
                script.src = scriptSrc;

                var scriptText = iframe[0].contentWindow.document.createElement('script');
                scriptText.type = "text/javascript";
                scriptText.innerHTML = "";

                script.onload = function () {
                    $.get(srcUrl, function (data) {
                        var baseTag = document.createElement('base');
                        baseTag.href = "";
                        iframe[0].contentWindow.document.head.appendChild(baseTag);
                        scriptText.innerHTML = "" +
                            "var obj = " + JSON.stringify(data) + ";\n" +
                            "var stage = new swiffy.Stage(document.getElementById('swiffycontainer'), obj);" +
                            "stage.start();";
                        iframe[0].contentWindow.document.body.appendChild(scriptText);

                    }, null, 'json');
                };

                iframe.contents().find('body').append($('<div id="swiffycontainer"></div>', {style: "width:" + dimensions.width + "px; height:" + dimensions.height + "px;"}));
                iframe[0].contentWindow.document.head.appendChild(script);
            });

            $(window).on('resize', this.debouncedResizeHandler());
        }
    });

    function MathJaxEngine() {
        this.init();
    }

    $.extend(MathJaxEngine.prototype, {
        debounceTimeout: 500,

        init: function () {
            MathJax.Hub.Queue(["Typeset", MathJax.Hub]);
            $(window).on('resize', this.debouncedResizeHandler());
        },

        mathJaxReload: function () {
            MathJax.Hub.Queue(["Rerender", MathJax.Hub]);
        },

        debouncedResizeHandler: function () {
            var _this = this;

            if (this._debounceHandler) {
                return this._debounceHandler;
            } else {
                this._debounceHandler = $.debounce(_this.debounceTimeout, (function () {
                    return function () {
                        _this.mathJaxReload();
                    };
                }()));

                return this._debounceHandler;
            }
        }
    });

    return {
        EngineInterface: EngineInterface,
        GeogebraEngine: GeogebraEngine,
        SwiffyEngine: SwiffyEngine,
        MathJaxEngine: MathJaxEngine
    };

})(epGlobal.jQuery);
epGlobal.companyNamespace = epGlobal.companyNamespace || {};

epGlobal.companyNamespace.engines = (function ($) {
    //import EngineInterface by class name with namespace
    var EngineInterface = epGlobal.common.engines.EngineInterface;

    //create new engine class
    function ExampleEngine(source, destination) {
        this.init(source, destination);
    }

    //extend our class with interface and add new behaviour on at least load() function
    $.extend(ExampleEngine.prototype, EngineInterface.prototype, {
        //this method must be implemented, it will be called when WOMI want to load object
        load: function () {
            var _this = this;
            this._loadEngineScript('example_engine', this.source, function () {
                _this.game = new epGlobal.companyNamespace.Game1.GameClass(_this.destination);
                _this.game.start();
            });
        },
        dispose: function () {
            if (this.game) {
                this.game.clean();
            }
        }
    });

    function ExampleEngine2(source, destination) {
        this.init(source, destination);
    }

    $.extend(ExampleEngine2.prototype, EngineInterface.prototype, {
        scriptSrc: "/static/3rdparty/ge/engine1/v1/js/scriptsloader.js",
        //this method must be implemented, it will be called when WOMI want to load object
        load: function () {
            //this.source => game_001/ form womi definition

            //add some containers
            var id = this.source.substring(0, this.source.length - 1);
            //calculate dimensions
            var height = this.destination.width();
            var container = $('<div id="' + id + '" style="overflow:hidden;text-align:center;">\
            <div class="bg-container" style="overflow:hidden;position:absolute;z-index:-1">\
                <img class="bg" src="' + this.source + 'images/drewno.jpg" style="width:1px;height:1px;z-index:1"/>\
            </div>\
            <canvas id="canvas" style="z-index:10;"></canvas>\
            </div>');
            this.destination.append(container);
            var _this = this;
            this.game = null;
            //load game engine scripts and then load scripts related with game
            //in this case we load scripts dynamically when loading WOMI, game engine probably will be load during module engine dependency checking
            //function loadGEScripts is part of script from scriptSrc and this script should have opportunity to load another scripts from engine
            this._loadScripts(this.scriptSrc, function () {
                loadGEScripts(function () {
                    _this._loadScripts(_this.source + 'js/game_scene.js', _this.source + 'js/animacja_macyka.js', function () {
                        $.getJSON(_this.source + 'config.json',function (response) {
                            _this.game = new ge.GameScene(container, response);
                        }).error(function (e) {
                                console.log("Blad wczytywania pliku konfiguracyjnego");
                            });
                    });
                });

            });
        },
        //this method should destroy game object and any other resources
        dispose: function () {
            if (this.game) {
                //this.game.clean();
            }
        }
    });


    return {
        ExampleEngine: ExampleEngine,
        ExampleEngine2: ExampleEngine2
    }
})(epGlobal.jQuery);



(function ($) {
    'use strict';

    Modernizr.load({
        test: Modernizr.touch,
        yep: ['/static/reader/js/libs/jquery.hammer.min.js', '/static/reader/js/touch.js']
    });

}(epGlobal.jQuery));

epGlobal.reader = epGlobal.reader || {};

epGlobal.reader.layout = (function ($) {
    'use strict';

    var fullScreenBreakpoint = 1023;

    function fullScreenMode() {
        return $(window).width() > fullScreenBreakpoint;
    }

    function responsiveSpans() {
        var bookIndex = $('.col-sidebar');

        if (fullScreenMode()) {
            bookIndex.show();
            $('.topbar').fadeIn();
        } else {
            bookIndex.hide();
        }
    }

    function toggleSidebar() {
        $('.col-sidebar').toggleClass('hide-sidebar');
        $('.col-main').toggleClass('center');
        $('.toggle-sidebar-button').children("i").toggleClass('icon-angle-right icon-angle-left');
    }

    function toggleNavbars() {
        $('.topbar, .bottombar').fadeToggle();
    }

    function WOMIMenuLayout() {
        this.init();
    }

    $.extend(WOMIMenuLayout.prototype, {
            init: function () {
                this._menu = $('<ul />');
                this._womiMenu = $('<div />', {
                    'class': 'womi-menu'
                });
                this._womiMenu.append(this._menu);
                this._menu.append(womiMenuButton(this._menu));
            },

            getMenu: function () {
                return this._womiMenu;
            },

            addMenuItem: function (item) {
                var li = $('<li />');
                var itemA = $('<button />', {
                    'class': item.name.replace(' ', '')
                });
                itemA.click(function () {
                    return item.callback();
                });
                li.append(itemA);
                this._menu.append(li);
            }
        }
    );

    function womiMenuButton(menu) {
        var womiMenuButton = $("<li><button><i class='icon-ellipsis-horizontal'></i></button></li>");

        var hidden = false;

        womiMenuButton.click(function () {
            if (hidden) {
                $(menu).find('li').show();
                hidden = false;
            } else {
                $(menu).find('li').hide().filter(':lt(1)').show();
                hidden = true;
            }
        });

        return womiMenuButton;
    }

    $(document).ready(function () {
        $('#toggle-index').click(function () {
            $('.col-sidebar').fadeToggle('fast', function () {
                $('body').toggleClass('stop-scrolling');
            });
        });

        womiMenuButton();
    });

    $(window).resize(function () {
        responsiveSpans();
    });

    return {
        fullScreenMode: fullScreenMode,
        WOMIMenuLayout: WOMIMenuLayout,
        toggleSidebar: toggleSidebar,
        toggleNavbars: toggleNavbars
    };

})(epGlobal.jQuery);



(function ($) {
    'use strict';

    function searchBox() {
        var searchArea = $('.search-area');
        var searchButton = $('.search-button');

        $('.grid').click(function () {
            searchArea.hide();
            searchButton.removeClass('selected');
        });

        $('.search-button, #search-input').click(function (event) {
            event.stopPropagation();
        });

        var showSearchInput = function () {
            searchArea.show();
            searchButton.addClass('selected');
        };

        searchArea.keypress(function (event) {
            // Enter
            if (event.which == 13) {
                searchButton.click();
            }
        });

        searchButton.click(function () {
            if (searchArea.is(':hidden')) {
                if (!Modernizr.touch) {
                    showSearchInput();
                } else {
                    $('html, body').animate({ scrollTop: 0 }, 'fast', function () {
                        showSearchInput();
                    });
                }
            } else {
                var q = searchArea.find('input[name=q]');
                window.location.href = searchArea.attr('data-search-href') + '?q=' + q.val();
            }
        });
    }

    $(document).ready(function () {
        searchBox();
    });

}(epGlobal.jQuery));



epGlobal.reader = epGlobal.reader || {};

epGlobal.reader.womi = (function ($) {
    'use strict';

    var classForName = epGlobal.common.base.stringToFunction;

    function createVideoPlayer(node, id, audioTracks, subtitles) {
        node.append($('<iframe width="640" height="360" src="http://www.youtube.com/embed/hqiNL4Hn04A?feature=player_embedded" frameborder="0" allowfullscreen></iframe>'));
    }

    function createAudioPlayer(node, id) {
    }

    function WOMIMenu() {
        this.init();
    }

    function WOMIContainer(element) {
        this.init(element);
    }

    function ClonedWOMIContainer(element) {
        this.init(element);
    }

    function WOMIContainerBase(element) {
        this.init(element);
    }

    function WOMIImageContainer(element) {
        this.init(element);
    }

    function WOMIFSImageContainer(element) {
        this.init(element);
    }

    function WOMIMovieContainer(element) {
        this.init(element);
    }

    function WOMIAudioContainer(element) {
        this.init(element);
    }

    function WOMIInteractiveObjectContainer(element) {
        this.init(element);
    }

    function MultipleWOMIContainer(elements) {
        this.init(elements);
    }

    function ClonedMultipleWOMIContainer(elements) {
        this.init(elements);
    }

    $.extend(WOMIMenu.prototype, {
        init: function () {
        },
        getMenu: function () {
        },
        addMenuItem: function (item) {
        }
    });

    $.extend(WOMIContainerBase.prototype, {
        CLASS_MAPPINGS: {
            'image-container': WOMIImageContainer,
            'movie-container': WOMIMovieContainer,
            'audio-container': WOMIAudioContainer,
            'interactive-object-container': WOMIInteractiveObjectContainer,
            'multiple': MultipleWOMIContainer
        },
        init: function (element) {
            this._mainContainerElement = $(element);
            this.menuItems = [];
            this._lookForBlocks();
            this._discoverContent();
            this._load();
        },
        _lookForBlocks: function () {
        },
        _discoverContent: function () {
        },
        _load: function () {
        },
        load: function () {
        },
        dispose: function () {
        },
        getFSElement: function () {
            return null;
        },
        _dispatchEvent: function (message, object) {
            var ev = new CustomEvent(message, {
                detail: object,
                bubbles: true,
                cancelable: true
            });
            this._mainContainerElement[0].dispatchEvent(ev);
        },
        getMenuItems: function () {
            var _this = this;
            return [
                {
                    name: '',
                    callback: function () {
                        _this.load();
                    }
                }
            ]
        }
    });

    $.extend(MultipleWOMIContainer.prototype, WOMIContainerBase.prototype, {
        DISPLAY_MODES: {
            '2d': 'primaryElement',
            '3d-anaglyph': 'secondaryElement'
        },
        _lookForBlocks: function () {
            var _this = this;
            $(this._mainContainerElement).each(function (index, element) {
                _this[_this.DISPLAY_MODES[$(element).data('display-mode')]] = element;
            });
            this._selectedElement = this.primaryElement;
        },
        _discoverContent: function () {
            var div = this.primaryElement;
            this.primaryElement.womiObj = new this.CLASS_MAPPINGS[div.className](div);
            div = this.secondaryElement;
            this.secondaryElement.womiObj = new this.CLASS_MAPPINGS[div.className](div);
        },
        dispose: function () {
            this._selectedElement.womiObj.dispose();
            //this.switcher.dispose();
        },
        switchToPrimary: function () {
            this._selectedElement.womiObj.dispose();
            this._selectedElement = this.primaryElement;
            this._selectedElement.womiObj.load();
        },
        switchToSecondary: function () {
            this._selectedElement.womiObj.dispose();
            this._selectedElement = this.secondaryElement;
            this._selectedElement.womiObj.load();
        },
        load: function () {
            this._selectedElement.womiObj.load();
        },
        getFSElement: function () {
            return this._selectedElement.womiObj.getFSElement();
        },
        getMenuItems: function () {
            var _this = this;
            return [
                {
                    name: '2d',
                    callback: function () {
                        _this.switchToPrimary();
                    }
                },
                {
                    name: '3d',
                    callback: function () {
                        _this.switchToSecondary();
                    }
                }
            ]
        }

    });

    $.extend(ClonedMultipleWOMIContainer.prototype, MultipleWOMIContainer.prototype, {
        CLASS_MAPPINGS: {
            'image-container': WOMIFSImageContainer,
            'movie-container': WOMIMovieContainer,
            'audio-container': WOMIAudioContainer,
            'interactive-object-container': WOMIInteractiveObjectContainer,
            'multiple': ClonedMultipleWOMIContainer
        }
    });

    $.extend(WOMIContainer.prototype, WOMIContainerBase.prototype, {

        _lookForBlocks: function () {
            this._classic = $(this._mainContainerElement.find('.classic'));
            this._mobile = $(this._mainContainerElement.find('.mobile'));
            this._selectedBlock = this._mobile.length && this._mobile.data('auto') && epGlobal.isMobile ? this._mobile : this._classic;
            this._lastClickedMenuItem = null;
        },
        _discoverContent: function () {
            //discover all blocks
            var _this = this;
            var divs = this._classic.children();
            //initialize content
            var className = this._resolveObjectClassType(divs);
            this._classic.womiObj = new this.CLASS_MAPPINGS[className](divs);
            this._classic.womiObj.getMenuItems().forEach(function (entry) {
                var item = {
                    name: 'classic' + entry.name,
                    callback: function () {
                        _this._lastClickedMenuItem = item;
                        _this.switchToClassic();
                        entry.callback();
                        return false;
                    }
                };
                _this.menuItems.push(item);
            });

            if (this._mobile.length) {
                divs = this._mobile.children();
                className = this._resolveObjectClassType(divs);
                this._mobile.womiObj = new this.CLASS_MAPPINGS[className](divs);
                this._mobile.womiObj.getMenuItems().forEach(function (entry) {
                    var item = {
                        name: 'mobile' + entry.name,
                        callback: function () {
                            _this._lastClickedMenuItem = item;
                            _this.switchToMobile();
                            entry.callback();
                            return false;
                        }
                    };
                    _this.menuItems.push(item);
                });
            }
            var fsItem = this._fullscreenMenuItem();
            if (fsItem) {
                this.menuItems.push(fsItem);
            }
            this.load();
        },
        _fullscreenMenuItem: function () {
            var _this = this;
            return {
                name: 'fullscreen',
                callback: function () {
                    var fsElement = _this._selectedBlock.womiObj.getFSElement();
                    if (fsElement != null) {
                        $.fancybox.open(fsElement.element, $.extend({
                            loop: false,
                            scrolling: 'no',
                            afterShow: function () {
                                if (fsElement.afterLoad) {
                                    fsElement.afterLoad();
                                }
                            },
                            onUpdate: function () {
                                if (fsElement.loaded) {
                                    $.fancybox.close(true);
                                } else {
                                    fsElement.loaded = true;
                                }
                            },
                            helpers: {
                                overlay: {
                                    locked: false
                                }
                            }
                        }, fsElement.options));
                    }
                    return false;
                }
            };
        },
        _getBetterFSSize: function (width, height) {
            var currW = typeof width === 'undefined' ? this._mainContainerElement.width() : width;
            var currH = typeof height === 'undefined' ? this._mainContainerElement.height() : height;
            var ratio = currW / currH;
            var props = {};
            props.padding = 3;
            props.margin = 3;
            var offset = 2 * props.margin + 2 * props.padding;
            if (ratio <= 1) {
                props.width = $(window).width() - offset;
                props.height = $(window).width() * ratio - offset;
            } else {
                props.height = $(window).height() - offset;
                props.width = $(window).height() * ratio - offset;
            }
            props.autoSize = false;
            //props.autoCenter = false;

            return props;
        },
        load: function () {
            this._selectedBlock.womiObj.load();
            this._generateMenu();
        },
        switchToClassic: function () {
            this._selectedBlock.womiObj.dispose();
            this._selectedBlock = this._classic;
            this._switchCallback();
        },
        switchToMobile: function () {
            this._selectedBlock.womiObj.dispose();
            this._selectedBlock = this._mobile;
            this._switchCallback();
        },
        _switchCallback: function () {
        },
        _resolveObjectClassType: function (divs) {
            if (divs.length > 1) {
                return 'multiple';
            } else {
                return divs[0].className;
            }
        },
        _generateMenu: function () {
            var clazz = classForName('epGlobal.reader.layout.WOMIMenuLayout');
            var womiMenu = new clazz();

            this.menuItems.forEach(function (entry) {
                womiMenu.addMenuItem(entry);
            });
            this._menuContainer = womiMenu.getMenu();
            this._mainContainerElement.append(this._menuContainer);
        }
    });

    $.extend(ClonedWOMIContainer.prototype, WOMIContainer.prototype, {
        CLASS_MAPPINGS: {
            'image-container': WOMIFSImageContainer,
            'movie-container': WOMIMovieContainer,
            'audio-container': WOMIAudioContainer,
            'interactive-object-container': WOMIInteractiveObjectContainer,
            'multiple': ClonedMultipleWOMIContainer
        },
        init: function (element) {

            this._mainContainerElement = $(element);
            this._mainContainerElement.css('margin', 0);
            this._mainContainerElement.children().each(function (index, element) {
                var el = $(element);
                if (!(el.hasClass('classic') || el.hasClass('mobile'))) {
                    el.remove();
                }
            });
            this.menuItems = [];
            this._lookForBlocks();
            this._discoverContent();
        },
        _fullscreenMenuItem: function () {
            return null;
        },
        loadCurrent: function (itemName) {
            this.menuItems.forEach(function (entry) {
                if (entry.name == itemName) {
                    entry.callback();
                }
            })
        }
    });


    $.extend(WOMIImageContainer.prototype, WOMIContainerBase.prototype, {
        MEDIA_MAPPINGS: {
            '480': '(max-width: 480px)',
            '980': '(max-width: 480px) and (-webkit-min-device-pixel-ratio: 1.5),(min-resolution: 144dpi)',
            '1440': '((max-width: 979px) and (-webkit-min-device-pixel-ratio: 2.0),(min-resolution: 192dpi)), ((min-width: 980px) and (-webkit-min-device-pixel-ratio: 1.5),(min-resolution: 144dpi))',
            '1920': '(min-width: 980px) and (-webkit-min-device-pixel-ratio: 2.0),(min-resolution: 192dpi)'
        },
        DEFAULT_MEDIA: 980,
        maxHeight: 0.8,
        _lookForBlocks: function () {
            //this._mainContainerElement = $(this._mainContainerElement[0]);
            var _this = this;
            this._availableResolutions = [];
            $(this._mainContainerElement.find('div')).each(function (index, element) {
                _this._availableResolutions.push($(element).data('resolution'));
            });

        },
        _discoverContent: function () {
            this._altText = this._mainContainerElement.data('alt');
            this._width = this._mainContainerElement.data('width');
            this._src = this._mainContainerElement.data('src');
        },
        _match: function (media) {
            return (window.matchMedia && window.matchMedia(media).matches);
        },
        _buildMediaUrl: function (root, entry) {
            var pattern = /=$/;
            var base = root;
            if (root.search(pattern) == -1) {
                pattern = /\/$/;
                base = base.replace(pattern, "");
                if (entry != "") {
                    var dotPos = base.lastIndexOf('.');
                    return base.substring(0, dotPos) + '-' + entry + base.substring(dotPos);
                } else {
                    return base;
                }
            }
            return base + entry;
        },

        load: function () {
            var _this = this;
            var selectedMedia = this.DEFAULT_MEDIA;
            this._availableResolutions.forEach(function (entry) {
                if (_this._match(_this.MEDIA_MAPPINGS[entry])) {
                    selectedMedia = entry;
                }
            });
            if (this._availableResolutions.length == 0) {
                selectedMedia = "";
            }
            if (this._mainContainerElement.find('img').length > 0 && !this._imgElement) {
                this._mainContainerElement.find('img').remove();
            }
            if (!this._imgElement) {
                this._imgElement = $('<img>', {
                    'class': 'generated-image',
                    alt: this._altText,
                    style: 'width: ' + this._width + ";",
                    src: this._buildMediaUrl(this._src, selectedMedia)
                });

                this._mainContainerElement.append(this._imgElement);
                $(window).on('resize', this._resize());
            }

        },
        dispose: function () {
            if (this._imgElement != null) {
                this._imgElement.remove();
                this._imgElement = null;
                $(window).off('resize', this._resize());
            }
        },
        getFSElement: function () {
            return { element: $('<img>', {
                src: this._buildMediaUrl(this._src, 1920)
            }), options: {
                fitToView: true,
                aspectRatio: true
            }};
        },
        _resize: function () {
            var _this = this;
            if (this._resizeHandler) {
                this._resizeHandler = function () {
                    $(_this._imgElement).css({maxHeight: _this.maxHeight * $(window).height()});
                }
            }
            return this._resizeHandler;
        }


    });

    $.extend(WOMIFSImageContainer.prototype, WOMIImageContainer.prototype, {
        MEDIA_MAPPINGS: {
            '1920': '(min-width: 100px)'
        },
        DEFAULT_MEDIA: 1920
    });

    $.extend(WOMIAudioContainer.prototype, WOMIContainerBase.prototype, {

        _discoverContent: function () {
            this._altText = this._mainContainerElement.data('alt');
            this._audioId = this._mainContainerElement.data('audio-id');
            this._width = this._mainContainerElement.data('width');
        },

        load: function () {
            if (this._mainContainerElement.find('.generated-av').length > 0 && !this._avElement) {
                this._mainContainerElement.find('.generated-av').remove();
            }
            if (!this._avElement) {
                this._avElement = $('<div />', {
                    'class': 'generated-av',
                    style: 'width: ' + (this._width ? this._width : '100%') + ";"
                });

                this._mainContainerElement.append(this._avElement);
                this._runMedia();
            }
        },
        _runMedia: function () {
            createAudioPlayer(this._avElement, this._audioId);
        },
        dispose: function () {
            if (this._avElement != null) {
                this._avElement.remove();
                this._avElement = null;
            }
        },
        getFSElement: function () {
            return { element: this._avElement.clone(), options: {} };
        }
    });

    $.extend(WOMIMovieContainer.prototype, WOMIAudioContainer.prototype, {
        _lookForBlocks: function () {
            //this._mainContainerElement = $(this._mainContainerElement[0]);

            this._audioTracksBlock = this._mainContainerElement.find('.audio-tracks');
            this._subtitlesBlock = this._mainContainerElement.find('.subtitles');
        },
        _discoverContent: function () {
            var _this = this;
            this._altText = this._mainContainerElement.data('alt');
            this._width = this._mainContainerElement.data('width');
            this._movieId = this._mainContainerElement.data('movie-id');
            this.audioTracks = null;
            this.subtitles = null;
            if (this._audioTracksBlock.length) {
                this.audioTracks = [];
                this._audioTracksBlock.find('div').each(function (index, element) {
                    _this.audioTracks.push({
                        text: $(element).data('text'),
                        value: $(element).data('value')
                    });
                });
            }
            if (this._subtitlesBlock.length) {
                this.subtitles = [];
                this._subtitlesBlock.find('div').each(function (index, element) {
                    _this.subtitles.push({
                        text: $(element).data('text'),
                        value: $(element).data('value')
                    });
                });
            }
        },
        _runMedia: function () {
            createVideoPlayer(this._avElement, this._movieId, this.audioTracks, this.subtitles);
        }
    });

    $.extend(WOMIInteractiveObjectContainer.prototype, WOMIContainerBase.prototype, {
        ENGINES: {"example_engine": {"url_template": "", "class_name": "epGlobal.companyNamespace.engines.ExampleEngine", "ignore_in_dependencies": true, "internal": true, "after_load_call": ""}, "ge_engine1": {"url_template": "3rdparty/ge/engine1/v1/js/scriptsloader.js", "class_name": "", "ignore_in_dependencies": false, "internal": true, "after_load_call": "loadGEScripts();"}, "example_engine2": {"url_template": "", "class_name": "epGlobal.companyNamespace.engines.ExampleEngine2", "ignore_in_dependencies": true, "internal": true, "after_load_call": ""}, "geogebra": {"url_template": "//www.geogebra.org/web/4.2/web/web.nocache.js", "class_name": "epGlobal.common.engines.GeogebraEngine", "ignore_in_dependencies": true, "internal": true, "after_load_call": ""}, "qml": {"url_template": "3rdparty/qml/1_0/qml.js", "class_name": "", "ignore_in_dependencies": false, "internal": true, "after_load_call": "initQml();"}, "mathjax": {"url_template": "3rdparty/mathjax/2.2-727332c/MathJax.js?config=MML_HTMLorMML,epo&locale=pl", "class_name": "", "ignore_in_dependencies": false, "internal": true, "after_load_call": "new epGlobal.common.engines.MathJaxEngine();"}, "swiffy": {"url_template": "https://www.gstatic.com/swiffy/v5.2/runtime.js", "class_name": "epGlobal.common.engines.SwiffyEngine", "ignore_in_dependencies": false, "internal": true, "after_load_call": ""}},

        _lookForBlocks: function () {
            this._interactiveObject = this._mainContainerElement.children()[0];
            if (this._mainContainerElement.children().length > 1) {
                this._replacementScreen = this._mainContainerElement.children()[1];
            }

        },
        _discoverContent: function () {
            var className = this._interactiveObject.className;
            this._width = this._mainContainerElement.data('width');
            this._heightRatio = this._mainContainerElement.data('height-ratio');
            this._source = "";
            this._engine = $(this._interactiveObject).data('object-engine');
            if (className == 'standard-interactive-object') {
                this._source = $(this._interactiveObject).data('object-src');
            } else if (className == 'resource-included-interactive-object') {
                this._source = $(this._interactiveObject).children()[0];
            }
            if (this._mainContainerElement.find('.generated-engine').length > 0) {
                this._mainContainerElement.find('.generated-engine').remove();
            }
            this._engineContainerTemplate = $('<div />', {
                'class': "generated-engine",
                style: 'width: ' + this._width + ";" + 'margin: 0 auto;'
            });
            this._engineContainer = this._engineContainerTemplate.clone();
            if (this._heightRatio) {
                this._engineContainer.data('height-ratio', this._heightRatio);
            }
            if (this._replacementScreen) {
                this._engineContainer.append($(this._replacementScreen).clone());
            }
            this._mainContainerElement.append(this._engineContainer);
            this._currentClazz = classForName(this.ENGINES[this._engine].class_name);
            this._engineHandler = new this._currentClazz(this._source, this._engineContainer);

        },
        load: function () {
            var children = this._engineContainer.children();
            if (children.length == 0 || (children.length == 1 && this._replacementScreen)) {
                this._engineHandler.load();
            }
        },
        dispose: function () {
            this._engineHandler.dispose();
        },
        _scaleElement: function (srcwidth, srcheight) {
            var props = $.fancybox.defaults;
            var offset = 2 * props.margin + 2 * props.padding;
            var result = { width: 0, height: 0, fScaleToTargetWidth: true };
            var targetWidth = $(window).width() - offset;
            var targetHeight = $(window).height() - offset;
            var fLetterBox = true;
            if ((srcwidth <= 0) || (srcheight <= 0) || (targetWidth <= 0) || (targetHeight <= 0)) {
                return result;
            }

            // scale to the target width
            var scaleX1 = targetWidth;
            var scaleY1 = (srcheight * targetWidth) / srcwidth;

            // scale to the target height
            var scaleX2 = (srcwidth * targetHeight) / srcheight;
            var scaleY2 = targetHeight;

            // now figure out which one we should use
            var fScaleOnWidth = (scaleX2 > targetWidth);
            if (fScaleOnWidth) {
                fScaleOnWidth = fLetterBox;
            }
            else {
                fScaleOnWidth = !fLetterBox;
            }

            if (fScaleOnWidth) {
                result.width = Math.floor(scaleX1);
                result.height = Math.floor(scaleY1);
                result.fScaleToTargetWidth = true;
            }
            else {
                result.width = Math.floor(scaleX2);
                result.height = Math.floor(scaleY2);
                result.fScaleToTargetWidth = false;
            }
            if (targetWidth < result.width) {
                result.width = targetWidth;
            }
            if (targetHeight < result.height) {
                result.height = targetHeight;
            }

            return result;
        },
        getFSElement: function () {
            var parentDiv = this._mainContainerElement.clone();
            var cloned = parentDiv.find('.generated-engine');
            cloned.children().remove();
            if (this._replacementScreen) {
                cloned.append($(this._replacementScreen).clone());
            }
            //set dimensions with ratio
            var dimensions = this._scaleElement(this._engineContainer.width(), this._engineContainer.height());
            cloned.width(dimensions.width);
            cloned.height(dimensions.height);
            var src = this._source;
            if (typeof this._source !== 'string') {
                src = $(this._source).clone()[0];
            }
            var _this = this;
            return {element: parentDiv,
                options: {},
                afterLoad: function () {
                    var toLoad = new _this._currentClazz(src, cloned);
                    toLoad.setFullScreenMode();
                    toLoad.load();
                } };
        }
    });

    var objList = [];

    function loadAllWOMI() {
        objList = [];

        $('.womi-container').each(function (index, element) {
            objList.push(new WOMIContainer(element));
        });

        epGlobal.handleSVGImages();
    }

    $(document).ready(function () {
        loadAllWOMI();

        /* We must include handleSVGImages here so to be sure that it is run after all WOMIs have been processed. */
        epGlobal.handleSVGImages();
    });

    return {
        load: loadAllWOMI,

        switchToMobile: function () {
            objList.forEach(function (entry) {
                entry.switchToMobile();
            });
        },
        WOMIImageContainer: WOMIImageContainer
    };

})(epGlobal.jQuery);

epGlobal.reader = epGlobal.reader || {};

epGlobal.reader.modules = (function ($) {
    'use strict';

    var HIGHLIGHT_TIME = 2500;

    $.fn.scrollTo = function (target, options, callback) {
        if (typeof options == 'function' && arguments.length == 2) {
            callback = options;
            options = target;
        }

        var settings = $.extend({
            scrollTarget: target,
            offsetTop: 50,
            duration: 10,
            easing: 'swing'
        }, options);

        return this.each(function () {
            var scrollPane = $(this);
            var scrollTarget = (typeof settings.scrollTarget == "number") ? settings.scrollTarget : $(settings.scrollTarget);
            var scrollY = (typeof scrollTarget == "number") ? scrollTarget : scrollTarget.offset().top + scrollPane.scrollTop() - settings.offsetTop;
            scrollPane.animate({scrollTop: scrollY }, settings.duration, settings.easing, function () {
                if (typeof callback == 'function') {
                    callback.call(this);
                }
            });
        });
    };

    var activeClass = 'link-active';

    function addLoadingIndicator() {
        var readerContent = $('.reader-content');

        $('#loading-indicator')
            .width(readerContent.outerWidth())
            .height($(window).height())
            .activity({segments: 12, height: 1});
    }

    function removeLoadingIndicator() {
        $('#loading-indicator')
            .activity(false);
    }

    function showIfCollapsed(moduleElement) {
        $('.collapse-x').each(function (index, element) {
            var found = false;

            if ($(element).has(moduleElement).length) {
                found = true;
            }

            if ($(element).attr('id') != 'index-menu') {
                if (found && !$(element).hasClass('in-x')) {
                    $(element).collapsex('show');
                }
            }
        });
    }

    function makeLinksAbsolute(windowHref) {
        // Remove anchor first from current window URL
        var anchorIdx = windowHref.lastIndexOf('#');
        if (anchorIdx != -1) {
            windowHref = windowHref.substr(0, anchorIdx);
        }

        $('#module-content').find('a').each(function () {
            var href = $(this).attr('href');

            if (!href) {
                return;
            }

            // Do not touch absolute links
            if (href.startsWith('//') || href.startsWith('http')) {
                return;
            }

            $(this).attr('href', windowHref + href);
        });
    }

    function loadModule(saveState, ajaxUrl, target, module_id, href, dependencies, fromClick) {
        var url = ajaxUrl;

        addLoadingIndicator();

        $.get(url, function (data) {
            $('#module-content').one('DOMNodeInserted', function () {
                epGlobal.reader.womi.load();

                makeLinksAbsolute(href);

                removeLoadingIndicator();
                window.scrollTo(0, 0);

                var module = $('a[data-module-id=' + module_id + ']');
                showIfCollapsed(module);

                if (!fromClick) {
                    scrollTableOfContentsTo(module);
                }
                generateAnchorsAndQRCode(href);
                resizeBreadcrumbs();
            });

            var base = $('#module-base');
            base.attr('href', base.data('base') + module_id);

            $(target).html(data);
            loadModuleDependencies(dependencies);

            if (saveState) {
                window.history.pushState({ajaxUrl: url,
                    module_id: module_id, href: href, dependencies: dependencies}, null, href);
            }
        });

        return false;
    }

    function loadModuleDependencies(url) {
        $.get(url, function (engines) {
            $.each(engines, function (index, object) {
                var head = $('head');
                if ($('script[src="' + object.url_template + '"]').length == 0) {
                    epGlobal.head.js(object.url_template, function () {
                        eval(object.after_load_call);
                    });
                } else {
                    eval(object.after_load_call);
                }
            });
        });
    }

    function updateNavigationButtons(current, size) {
        var DISABLED_CLASS = 'disabled';
        var nextBtn = $('.next-btn');
        var prevBtn = $('.prev-btn');

        if (current + 1 >= size || size <= 1) {
            nextBtn.addClass(DISABLED_CLASS);
        } else {
            nextBtn.removeClass(DISABLED_CLASS);
        }

        if (current - 1 < 0 || size <= 1) {
            prevBtn.addClass(DISABLED_CLASS);
        } else {
            prevBtn.removeClass(DISABLED_CLASS);
        }
    }

    function scrollTableOfContentsTo(element) {
        if (element == null) {
            return;
        }

        var area = $('.scrollable-area');

        if (area.data('jsp')) {
            var api = area.data('jsp');
            api.scrollToElement(element);
        } else {
            area.scrollTo(element, {offsetTop: area.offset().top + 10});
        }
    }

    function goToPrevOrNextModule(offset) {
        var modules = $('#index-menu').find('.module-a');
        var foundIdx = -1000;

        modules.each(function (index, element) {
            if ($(element).hasClass(activeClass)) {
                foundIdx = index;
                return false;
            }
            return true;
        });

        foundIdx = foundIdx + offset;

        var m = null;
        if (foundIdx < modules.length && foundIdx >= 0) {
            m = $(modules.get(foundIdx));
            $(m).loadModule(false);
        }

        updateNavigationButtons(foundIdx, modules.length);
        return m;
    }

    function setActiveClass(modules, thisModule) {
        var moduleIdx = 0;
        var moduleElement;

        modules.each(function (index, element) {
            if ($(element)[0] != $(thisModule)[0] && $(element).hasClass(activeClass)) {
                $(element).removeClass(activeClass);
            } else if ($(element)[0] == $(thisModule)[0] && !$(element).hasClass(activeClass)) {
                $(element).addClass(activeClass);
                moduleIdx = index;
                moduleElement = element;
            }
        });

        updateNavigationButtons(moduleIdx, modules.length);
    }

    function makeBreadcrumbs(moduleElement) {
        var elem = moduleElement;
        var breadcrumbs = $('.breadcrumbs');

        $('.removable-crumb').remove();

        var reverseCrumbs = [];
        var id = [];

        while ($(elem).attr('id') != 'index-menu') {
            if ($(elem).is('a')) {
                reverseCrumbs.push($(elem).text());
            }

            if ($(elem).is('li')) {
                if (typeof $(elem).attr('data-coll-id') === 'undefined')
                    id.push(-1);
                else
                    id.push($(elem).attr('data-coll-id'));
            }

            if ($(elem).is('ul')) {
                elem = elem.prev();
            } else {
                elem = $(elem).parent();
            }
        }

        for (var i = reverseCrumbs.length - 1; i >= 0; i--) {
            var li = $('<li/>', {
                'class': 'removable-crumb' + (i == 0 ? ' current' : ''),
                'html': $('<a/>', {
                    text: reverseCrumbs[i]
                }),
                'data-coll-number': id[i],
                'click': function (event) {
                    clickBreadcrumb(event);
                }
            });

            li.appendTo(breadcrumbs);
        }
    }

    function clickBreadcrumb(event) {
        var object = $(event.target);
        var data_coll_number = null;

        if (object.is('a'))
            data_coll_number = object.parent().attr('data-coll-number');
        else
            data_coll_number = object.attr('data-coll-number');

        if (data_coll_number == -1) {
            window.scrollTo(0, 0);
        } else {
            var menu = $('#index-menu');
            var collection = menu.find("[data-coll-id='" + data_coll_number + "']");
            collection.find('.module-a')[0].click();
        }
    }

    function addNavigationHandlers() {
        $('.prev-btn').click(function () {
            goToPrevOrNextModule(-1);
        });

        $('.next-btn').click(function () {
            goToPrevOrNextModule(1);
        });

        $(document).keydown(function (event) {
            if (event.keyCode == 37) {
                goToPrevOrNextModule(-1);
                event.preventDefault();
            } else if (event.keyCode == 39) {
                goToPrevOrNextModule(1);
                event.preventDefault();
            }
        });
    }

    function fullHrefToCurrentModule() {
        // var href = window.location.href;
        // 
        // if (href.lastIndexOf('/m/') == -1) {
        //     href += '/m/' + $('#module-base').data('module-id');
        // }
        // 
        // return href;
    }

    function getElementWithId(object) {
        var element = $(object);

        while (!element.attr('id')) {
            element = element.parent();
        }

        return element;
    }

    function generateAnchorsAndQRCode(oldHref) {
        var href = oldHref;
        var index = href.indexOf('#');
        var anchor = '';

        if (index > 0) {
            anchor = href.substring(index);
        }

        index = index != -1 ? index : href.length;
        href = href.substr(0, index);

        var headers = $('#module-content').find('.section-header');

        var showQRCode = function (event) {

            var element = getElementWithId(event.target);
            var div = $('<div/>', {
                'class': 'qr-code'
            });
            div.qrcode(href + '#' + element.attr('id'));
            $.fancybox({
                content: div,
                wrapCSS: 'fancybox-modal fancybox-modal-qr',
                modal: true,
                beforeShow: function () {
                    $('body').click(function () {
                        $.fancybox.close();
                    });
                },
                helpers: {
                    overlay: {
                        locked: false,
                        css: {
                            'background': 'rgba(255, 255, 255, 0.6)'
                        }
                    }
                }
            });
        };

        headers.each(function (index, element) {
            var elemen = getElementWithId(element);
            var container = $('<div/>', {
                class: 'anchor-container'
            });
            var a = $('<a/>', {
                'href': href + '#' + elemen.attr('id'),
                'html': '<i class="icon-anchor"</i>',
                'class': 'anchor',
                click: function () {
                    elemen.effect('highlight', {}, HIGHLIGHT_TIME);
                }
            });

            container.append(a);
            elemen.addClass('anchor-padding');

            var qra = $('<a/>', {
                href: '#',
                'html': '<i class="icon-qrcode"</i>',
                'class': 'qr-anchor',
                'click': function (event) {
                    showQRCode(event);
                    return false;
                }
            });

            container.append(qra);

            $(element).append(container);
        });

        function mouse_triger() {
            $(this).find('.anchor-container').fadeToggle();
        }

        $('.section-header').hoverIntent({
            over: mouse_triger,
            interval: 300,
            timeout: 1000,
            out: mouse_triger
        });

        if (anchor != '') {
            setTimeout(function () {
                $(anchor).effect('highlight', {}, HIGHLIGHT_TIME);
            }, 500);
        }
    }

    $(document).ready(function () {
        var modules = $('.module-a');
        var base = $('#module-base');

        addNavigationHandlers();

        $.fn.loadModule = function (fromClick) {
            if (!epGlobal.reader.layout.fullScreenMode()) {
                $(".col-sidebar").fadeOut('fast', function () {
                    $('body').removeClass('stop-scrolling');
                });
            }

            setActiveClass(modules, this);
            makeBreadcrumbs(this);

            return loadModule(true, $(this).data('ajax-url'), '#module-content',
                $(this).data('module-id'), $(this).attr('href'), $(this).data('dependencies-url'), fromClick);
        };

        modules.click(function () {
            return $(this).loadModule(true);
        });

        window.onpopstate = function (event) {
            if (event.state != null) {
                var state = event.state;
                setActiveClass(modules, $('a[data-module-id=' + state.module_id + ']'));
                loadModule(false, state.ajaxUrl, '#module-content', state.module_id, state.href, state.dependencies, true);
            }
        };

        window.history.replaceState({
                ajaxUrl: base.data('ajax-url'),
                module_id: base.data('module-id'),
                href: fullHrefToCurrentModule(),
                dependencies: base.data('dependencies-url')
            },
            null,
            fullHrefToCurrentModule());

        loadModuleDependencies(base.data('dependencies-url'));

        var moduleIdx = 0;

        modules.each(function (index, element) {
            if ($(element).data('module-id') == base.data('module-id')) {
                $(element).addClass(activeClass);
                moduleIdx = index;
                makeBreadcrumbs(element);
                return false;
            }
            return true;
        });

        updateNavigationButtons(moduleIdx, modules.length);
        makeLinksAbsolute(window.location.href);

        generateAnchorsAndQRCode(window.location.href);
        resizeBreadcrumbs();
    });

    $(window).resize(function () {
        resizeBreadcrumbs();
    });

    function getWidthWithPaddings(element) {
        return element.outerWidth() + element.outerWidth() - element.width();

    }

    function getWidthOfBreadcrumbs(breads) {
        var widthOfBreads = 0;
        breads.each(function (index, element) {
            widthOfBreads += getWidthWithPaddings($(element));
        });
        return widthOfBreads;
    }

    function resizeBreadcrumbs() {
        var bar = $('.topbar-inner');
        var breads = bar.find('.removable-crumb:visible');
        // last '-1' is neccesary for firefox where toolbar disappear if breadcrumbs are ellipsis
        var widthForBreadcrumbs = bar.outerWidth() - bar.find('.home').outerWidth(true) - bar.find('.topbar-tools').outerWidth(true) - 1;

        var widthOfBreads = getWidthOfBreadcrumbs(breads);
        var maxWidth = [];

        var MIN_WIDTH = 56;

        if (widthOfBreads < widthForBreadcrumbs) {
            breads.each(function (index, element) {
                maxWidth[index] = getWidthWithPaddings($(element));
            });
        } else {
            var lastBreadcrumnbWidth = getWidthWithPaddings($(breads[breads.length - 1]));
            var widthWithoutLast = widthForBreadcrumbs - lastBreadcrumnbWidth;
            var minValue = (breads.length - 1) * MIN_WIDTH;

            if (widthWithoutLast > minValue) {
                for (var i = 0; i < breads.length - 1; i++) {
                    maxWidth[i] = widthWithoutLast / (breads.length - 1);
                }
                maxWidth[breads.length - 1] = lastBreadcrumnbWidth;
            } else {
                for (var i = 0; i < breads.length - 1; i++) {
                    maxWidth[i] = MIN_WIDTH;
                }
                maxWidth[breads.length - 1] = widthForBreadcrumbs - minValue;
            }
        }

        breads.each(function (index, element) {
            $(element).css('maxWidth', maxWidth[index]);
        });
    }

    return {
    };

}(epGlobal.jQuery));

(function ($) {
    'use strict';

    var LEGEND_ALREADY_SHOWN = 'epo_legendAlreadyShown';

    var legend_path_desktop = $('#legend-data').data('legend-path-desktop');
    var legend_path_mobile = $('#legend-data').data('legend-path-mobile');

    $(document).ready(function () {
            if (epGlobal.isDesktop)
                showDesktopLegend();
            else
                showMobileLegend();
        }
    );

    function legendAlreadyShown() {
        if (window.localStorage) {
            return Boolean(window.localStorage[LEGEND_ALREADY_SHOWN]) || false;
        }
        return true;
    }

    function resizeFancybox() {
        var container = $('#legend-container');
        container.height($(window).height());
        container.width($(window).width());
    }

    function showMobileLegend() {
        if (legendAlreadyShown() || !legend_path_mobile)
            return;

        $.fancybox({
            type: 'ajax',
            href: legend_path_mobile,
            autoCenter: true,
            wrapCSS: 'legend-fancybox',
            autoResize: true,
            helpers: {
                overlay: {
                    closeClick: false
                }
            },
            beforeLoad: function () {
                $(window).bind('resize', resizeFancybox);
            },
            beforeClose: function () {
                if (window.localStorage) {
                    window.localStorage[LEGEND_ALREADY_SHOWN] = true;
                }
                $(window).unbind('resize', resizeFancybox);
            },
            beforeShow: function () {
                var legendMobile = $('#legend-mobile');
                legendMobile.height($(window).height());
                legendMobile.width($(window).width());

                var actual = $(legendMobile.children()[0]);
                var leftArrow = $('#legend-left');
                var rightArrow = $('#legend-right');

                leftArrow.addClass('no-clickable');

                leftArrow.click(function () {
                    if (leftArrow.hasClass('no-clickable')) {
                        return false;
                    }

                    actual = prevSlide(actual);
                    if (isFirst(actual))
                        leftArrow.addClass('no-clickable');

                    if (rightArrow.hasClass('no-clickable'))
                        rightArrow.removeClass('no-clickable');

                    return false;
                });

                rightArrow.click(function () {
                    if (rightArrow.hasClass('no-clickable')) {
                        return false;
                    }

                    actual = nextSlide(actual);

                    if (isLast(actual))
                        rightArrow.addClass('no-clickable');

                    if (leftArrow.hasClass('no-clickable'))
                        leftArrow.removeClass('no-clickable');

                    return false;
                });
            }
        })
    }

    function prevSlide(actual) {
        actual.removeClass('current');
        actual = actual.prev();
        actual.addClass('current');
        return actual;
    }

    function nextSlide(actual) {
        actual.removeClass('current');
        actual = actual.next();
        actual.addClass('current');
        return actual;
    }

    function isLast(element) {
        return element.next().length == 0;
    }

    function isFirst(element) {
        return element.prev().length == 0;
    }

    function showDesktopLegend() {
        if (legendAlreadyShown() || !legend_path_desktop)
            return;

        var timeout = setTimeout(function () {
            $.fancybox.close();
        }, 5000);

        $.fancybox({
            type: 'ajax',
            href: legend_path_desktop,
            closeBtn: false,
            helpers: {
                overlay: {
                    locked: false,
                    css: {
                        'background-color': 'rgba(255,255,255,0);'
                    }
                }
            },
            wrapCSS: 'legend-fancybox',
            beforeShow: function () {
                $('.fancybox-wrap').click(function () {
                    clearTimeout(timeout);
                    $.fancybox.close();
                });
            },
            beforeClose: function () {
                if (window.localStorage) {
                    window.localStorage[LEGEND_ALREADY_SHOWN] = true;
                }
            }
        });
    }
})(epGlobal.jQuery);


(function ($) {
    'use strict';

    var scrollableArea = $('.scrollable-area');
    var settings = {
        autoReinitialise: true
    };

    var jquerymousewheel = '/static/reader/js/libs/jquery.mousewheel.min.js';
    var jqueryjscrollpane = '/static/reader/js/libs/jquery.jscrollpane-mssupport.min.js';

    Modernizr.load({
        test: Modernizr.cssscrollbar,
        nope: [jquerymousewheel, jqueryjscrollpane],
        callback: function (url, result, key) {
            if (url === jqueryjscrollpane) {
                scrollableArea.jScrollPane(settings);
            }
        }
    });

}(epGlobal.jQuery));

(function ($) {
    'use strict';

    $.fn.collapsex = function (option) {
        return this.each(function () {
            var $this = $(this);

            var collapsed = !$this.hasClass('in-x');
            var opt = option;

            if (opt == 'toggle') {
                opt = (collapsed ? 'show' : 'hide');
            }

            if (opt == 'show' && collapsed) {
                $this.slideDown(200);
                $this.addClass('in-x');
            }

            if (opt == 'hide' && !collapsed) {
                $this.slideUp(200);
                $this.removeClass('in-x');
            }

            var a = $($this.prev());
            a[collapsed ? 'removeClass' : 'addClass']('collapsed');
        });
    };

    $(document).ready(function () {
        var collapsibleElements = $('[data-toggle=collapse-x]');

        collapsibleElements.each(function () {
            var target = $($(this).attr('data-target'));
            target.slideUp(0);
        });

        collapsibleElements.on('click', function (e) {
            var target = $($(this).attr('data-target'));
            target.collapsex('toggle');
        });

        $('.module-a').each(function (index, element) {
            if ($(element).data('module-id') == $('#module-base').data('module-id')) {
                var moduleElement = element;

                $('.collapse-x').each(function (index, element1) {
                    var found = false;

                    if ($(element1).has(moduleElement).length) {
                        found = true;
                    }

                    if ($(element1).attr('id') != 'index-menu') {
                        if (found && !$(element1).hasClass('in-x')) {
                            $(element1).collapsex('show');
                        }
                    }
                });
            }
        });
    });

})(epGlobal.jQuery);

(function ($) {
    'use strict';

    $(document).ready(function () {
        $('.toggle-sidebar').click(function () {
            epGlobal.reader.layout.toggleSidebar();
        });
    });

    $(document).on('click', '.check-button', function () {
        $('.correct-answer, .wrong-answer, .info-answer').hide();

        var value = $('input:radio[name=optionsRadios]:checked').val();
        var quizNumber = $(this).parent().attr('id');

        if (quizNumber == 'm291') {
            var ans = 'option2';
        } else if (quizNumber == 'm292') {
            var ans = 'option1';
        }

        if (value == ans) {
            $('.correct-answer').slideDown('fast');
        } else if (typeof value === 'undefined') {
            $('.info-answer').slideDown('fast');
        } else {
            $('.wrong-answer').slideDown('fast');
        }
    });

})(epGlobal.jQuery);
