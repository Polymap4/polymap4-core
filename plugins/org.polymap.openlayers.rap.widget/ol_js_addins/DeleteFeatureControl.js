OpenLayers.Util.extend($0, {
    initialize: function(options) {
        this.EVENT_TYPES = OpenLayers.Control.prototype.EVENT_TYPES.concat(['$2']);
        OpenLayers.Control.prototype.initialize.apply(this, [options]);
        this.layer = $1; 
        this.handler = new OpenLayers.Handler.Feature( 
            this, this.layer, {click: this.clickFeature} 
        );
        //this.handler.activate();
    },
    clickFeature: function(feature) { 
        // if feature doesn't have a fid, destroy it 
        if (feature.fid == undefined) {
            this.layer.destroyFeatures([feature]); 
        } 
        else {
            alert(feature + ' - ' + this.layer);
            feature.state = OpenLayers.State.DELETE; 
            this.events.triggerEvent( '$2', {feature: feature}); 
            this.layer.destroyFeatures([feature]);
            
//            feature.renderIntent = 'select';
//            this.layer.drawFeature(feature); 
        }
    },
    activate: function() {
        this.handler.activate();
    },
    deactivate: function() {
        this.handler.deactivate();
    },
    setMap: function(map) { 
        this.handler.setMap(map); 
        OpenLayers.Control.prototype.setMap.apply(this, arguments); 
    },
    CLASS_NAME: 'OpenLayers.Control.DeleteFeature' 
});
$0.initialize();
