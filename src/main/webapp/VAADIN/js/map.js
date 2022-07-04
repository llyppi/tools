    function initMap(lt,lg) {       
//        alert(loc);

        let map = false;
//        alert(lt);
//        alert(lg);
        let lagos = { lat: lt, lng: lg };
        
        var mapa = document.getElementById('mapa');
        
        map = new google.maps.Map(mapa, {
            zoom: 8,            
            center: lagos
        });

        google.maps.event.addListener(map, 'click', function (event) {            
            
            addMarker(event.latLng,map,null);
        });
        

//        map.addListener('dragend', function () {
//            
////            mapa.$server.setPosition(event.label,event.latLng.lat,event.latLng.lng);
//            
//        });
         
//        map.addListener('center_changed', function() {
//            window.setTimeout(function() {
//                map.panTo(marker.getPosition());
//                alert('center_changed');
//              }, 3000);
//          });

//        map.event.addListener('drag', function (event) {
//            alert(event);
//            
//        });
        // Add a marker at the center of the map.
//        if(loc!==null){
//            for (var i = 0; i < loc.length; i++) {
//
////                alert(lt);
////                alert(loc[i].lat);
//                lagos = { lat: loc[i].lat, lng: loc[i].lng };
//                addMarker(lagos, map);
//            }
//        }
//        addMarker(lagos, map);

        var link = document.getElementById('link');
        link.style.display = 'none';
        
        return map;
    }
    
     function addMarker(location,map,file) {
//        alert(location.lat+' '+location.lng);
//        alert(file);
        
//        latitude = JSON.parse(JSON.stringify(location)).lat;
//        longitude = JSON.parse(JSON.stringify(location)).lng;
        var image = '';
       
        if(file!==null){
            image = window.location.origin+'/tools/VAADIN/photos/'+file;
        }else{
            image = window.location.origin+'/tools/VAADIN/photos/Location24x24.png';
        }
        
        var marker = new google.maps.Marker({
            position: location,
            label: file,
            map: map,
            draggable: true,
            animation: google.maps.Animation.DROP,
            icon: image            
        });               
        
        marker.addListener('click', function() {
            map.setZoom(map.getZoom()+5);
            map.setCenter(marker.getPosition());
          });
          
        //var mapa = document.getElementById('mapa');
        
        marker.addListener('dragend', function(event) {
            var link = document.getElementById('link');
            var lat = JSON.parse(JSON.stringify(event)).latLng.lat;
            var lng = JSON.parse(JSON.stringify(event)).latLng.lng;
            
            var href = link.firstElementChild.firstElementChild.innerHTML;
            link.firstElementChild.href = href +'(\''+file+'\','+lat+','+lng+')';
            
            link.firstElementChild.click();            
//                mapa.$server.setPosition(file,lat,lng);
        });
//        var elems = document.getElementsByTagName('img');  
//        for (var i = 0; i < elems.length; i++) {
//            elems[i].style.borderRadius = '50%';
//        }
    }

