
/**
 * 
 */
OpenLayers.Handler.RightDrag = OpenLayers.Class( OpenLayers.Handler.Drag, {
    
    initialize: function( control, callbacks, options ) {
        OpenLayers.Handler.Drag.prototype.initialize.apply( this, arguments );
    },
    
    /**
     * Method: dragstart
     * This private method is factorized from mousedown and touchstart methods
     *
     * Parameters:
     * evt - {Event} The event
     *
     * Returns:
     * {Boolean} Let the event propagate.
     */
    dragstart: function( evt ) {
        var propagate = true;
        this.dragging = false;
        if (this.checkModifiers( evt ) &&
                (OpenLayers.Event.isRightClick( evt ) || OpenLayers.Event.isSingleTouch( evt ) )) {
            this.started = true;
            this.start = evt.xy;
            this.last = evt.xy;
            OpenLayers.Element.addClass( this.map.viewPortDiv, "olDragDown" );
            this.down( evt );
            this.callback( "down", [evt.xy] );
    
            OpenLayers.Event.stop( evt );
    
            if(!this.oldOnselectstart) {
                this.oldOnselectstart = document.onselectstart ?
                        document.onselectstart : OpenLayers.Function.True;
            }
            document.onselectstart = OpenLayers.Function.False;
    
            propagate = !this.stopDown;
        } 
        else {
            this.started = false;
            this.start = null;
            this.last = null;
        }
        return propagate;
    },
    
    CLASS_NAME: "OpenLayers.Handler.RightDrag"
});


/**
 * 
 */
OpenLayers.Handler.ContextMenuBox = OpenLayers.Class( OpenLayers.Handler.Box, {

    initialize: function( control, callbacks, options ) {
        OpenLayers.Handler.Box.prototype.initialize.apply( this, arguments );
        this.dragHandler = new OpenLayers.Handler.RightDrag(
            this,
            {
                down: this.startBox,
                move: this.moveBox,
                out: this.removeBox,
                up: this.endBox
            },
            {keyMask: this.keyMask}
        );
    },
    
    CLASS_NAME: "OpenLayers.Handler.ContextMenuBox"
});


/**
 * 
 */
OpenLayers.Control.ContextMenuControl = OpenLayers.Class( OpenLayers.Control, {

    initialize: function( options ) {
//        this.EVENT_TYPES = OpenLayers.Control.prototype.EVENT_TYPES.concat(
//                [\"" + EVENT_BOX \"] );
        alert( 'ContextMenuControl: ' + options );
        OpenLayers.Control.prototype.initialize.apply( this, [options] );
    },
    
    draw: function() {
        alert( 'draw(): ...' );
        this.map.div.oncontextmenu = function( e ) {
            e = e ? e : window.event;
            if (e.preventDefault) 
                e.preventDefault(); // For non-IE browsers.
            else 
                return false; // For IE browsers.
        };
        
        // this Handler.Box will intercept the shift-mousedown
        // before Control.MouseDefault gets to see it
        this.box = new OpenLayers.Handler.ContextMenuBox( this, { 'done': this.notice } );
        this.box.activate();
    },
    
    activate: function() {
        this.box.activate();
    },
    
    deactivate: function() {
        this.box.deactivate();
    },
    
    notice: function( bounds ) {
        var minXY = this.map.getLonLatFromPixel( new OpenLayers.Pixel( bounds.left, bounds.bottom ) );
        var maxXY = this.map.getLonLatFromPixel( new OpenLayers.Pixel( bounds.right, bounds.top ) );
        var bbox = new OpenLayers.Bounds( minXY.lon, minXY.lat, maxXY.lon, maxXY.lat );
        alert( bbox );
        this.events.triggerEvent('" + EVENT_BOX ', {bbox: bbox});
    },
    
    CLASS_NAME: "OpenLayers.Handler.ContextMenuControl"
});
