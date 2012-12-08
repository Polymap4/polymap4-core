
var body = document.body; //getElementById("dropbox")
 
// init event handlers
if (body.addEventListener) {
    body.addEventListener( "dragenter", noopHandler, false );
    body.addEventListener( "dragexit", noopHandler, false );
    body.addEventListener( "dragover", noopHandler, false );
    body.addEventListener( "drop", dropHandler, false );
}
   
function dropHandler( ev ) {
    
    // upload files
    var files = ev.dataTransfer.files;
    if (files.length > 0) {
        showWaitHint();
        var uploaded = 0;
        for (var i=0; i<files.length; i++) {
            //alert( 'Sending file: ' + files[i].name );
            sendFile( files[i], function( xhr ) {
                if (xhr.readyState == 4) {
                    if (++uploaded >= files.length) {
                        hideWaitHint();
                        endTransmission();
                    }
                }
            });
        }
    }
    // send eot with parameter
    else {
        var eventText = ev.dataTransfer.getData( 'Text' );
        endTransmission( eventText );
    }
}

function endTransmission( eventText ) {
    //qx.client.Timer.once( this._showWaitHint, this, 500 );
    setTimeout( function() {
        // signal end of transmission
        var xhr = (window.XMLHttpRequest)
            ? new XMLHttpRequest()  // code for IE7+, Firefox, Chrome, Opera, Safari
            : new ActiveXObject( 'Microsoft.XMLHTTP' ); // code for IE6, IE5
        var url = '?custom_service_handler=org.polymap.core.DndServiceHandler';
        xhr.open( 'POST', url, true );
        xhr.setRequestHeader( 'Content-type', 'application/x-www-form-urlencoded' ); 
        xhr.setRequestHeader( 'Connection', 'close' );
        var params = eventText ? 'eventText=' + encodeURIComponent( eventText ) : '';
        xhr.send( params );    
        
        // force UI update?
        qx.ui.core.Widget.flushGlobalQueues();
        var req = org.eclipse.swt.Request.getInstance();
        req.enableUICallBack();
    }, 500 );
}

function sendFile( file, callback ) {
    var xhr = (window.XMLHttpRequest)
        ? new XMLHttpRequest()  // code for IE7+, Firefox, Chrome, Opera, Safari
        : new ActiveXObject( 'Microsoft.XMLHTTP' ); // code for IE6, IE5

    var upload = xhr.upload;
 
    var url = 'dndupload?filename=' + file.name;
    xhr.open( 'POST', url, true );
    xhr.setRequestHeader( 'X-Filename', file.name );
    xhr.setRequestHeader( 'Content-type', file.type );
    xhr.setRequestHeader( 'Content-length', file.size );
    xhr.setRequestHeader( 'Connection', 'close' );
    xhr.onreadystatechange = function () {
        callback.call( {}, xhr );
    };
    xhr.send( file );    
}

/*function request( params ) {
    var xhr = (window.XMLHttpRequest)
        ? new XMLHttpRequest()  // code for IE7+, Firefox, Chrome, Opera, Safari
        : new ActiveXObject( 'Microsoft.XMLHTTP' ); // code for IE6, IE5

    var upload = xhr.upload;
 
    var url = 'dndupload?filename=' + file.name;
    xhr.open( 'POST', url, true );
    xhr.setRequestHeader( 'X-Filename', file.name );
    xhr.setRequestHeader( 'Content-type', file.type );
    xhr.setRequestHeader( 'Content-length', file.size );
    //xhr.setRequestHeader( 'Connection', 'close' );
    xhr.send( file );    
}*/

function showWaitHint() {
    var doc = qx.ui.core.ClientDocument.getInstance();
    doc.setGlobalCursor( qx.constant.Style.CURSOR_PROGRESS );
}

function hideWaitHint() {
    var doc = qx.ui.core.ClientDocument.getInstance();
    doc.setGlobalCursor( null );
}
    
function noopHandler( ev ) {
    ev.stopPropagation();
    ev.preventDefault();
}
