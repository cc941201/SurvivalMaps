//
//  ViewController.swift
//  Survival
//
//  Created by 张国晔 on 2016/10/5.
//  Copyright © 2016年 Johns Hopkins University. All rights reserved.
//

import UIKit
import MapKit

class ViewController: UIViewController, MKMapViewDelegate, StateDelegate {
    @IBOutlet var mapView: MKMapView! {
        didSet {
            let osmOverlay = MKTileOverlay(urlTemplate: "https://maps.wikimedia.org/osm-intl/{z}/{x}/{y}.png")
            osmOverlay.maximumZ = 18
            osmOverlay.canReplaceMapContent = true
            mapView.add(osmOverlay, level: .aboveRoads)
            
            if let url = server.url {
                let overlay = MKTileOverlay(urlTemplate: "\(url)/v1/heatmap/{x}-{y}-{z}.png")
                overlay.maximumZ = 12
                mapView.add(overlay, level: .aboveLabels)
            }
            
            let i0 = Double(75250) / 262144 * 360 - 180
            let i1 = Double(75350) / 262144 * 360 - 180
            let j0d = atan(sinh(.pi * (1 - 2 * Double(99850) / 262144)))
            let j0 = j0d * 180 / .pi
            let j1d = atan(sinh(.pi * (1 - 2 * Double(99950) / 262144)))
            let j1 = j1d * 180 / .pi
            for i in 75250..<75350 {
                let longitude = Double(i) / 262144 * 360 - 180
                let points = [CLLocationCoordinate2D(latitude: j0, longitude: longitude), CLLocationCoordinate2D(latitude: j1, longitude: longitude)]
                let line = MKPolyline(coordinates: points, count: 2)
                mapView.add(line, level: .aboveLabels)
            }
            for j in 99850..<99950 {
                let latitudeDegree = atan(sinh(.pi * (1 - 2 * Double(j) / 262144)))
                let latitude = latitudeDegree * 180 / .pi
                let points = [CLLocationCoordinate2D(latitude: latitude, longitude: i0), CLLocationCoordinate2D(latitude: latitude, longitude: i1)]
                let line = MKPolyline(coordinates: points, count: 2)
                mapView.add(line, level: .aboveLabels)
            }
            
            mapView.mapType = .satellite
            mapView.delegate = self
        }
    }
    @IBOutlet weak var fromTextField: UITextField! {
        didSet {
            let fromLabel = UILabel()
            fromLabel.text = "From: "
            fromLabel.font = UIFont.preferredFont(forTextStyle: .title1)
            fromLabel.sizeToFit()
            fromTextField.leftView = fromLabel
            fromTextField.leftViewMode = .always
        }
    }
    @IBOutlet weak var toTextField: UITextField! {
        didSet {
            let toLabel = UILabel()
            toLabel.text = "To: "
            toLabel.font = UIFont.preferredFont(forTextStyle: .title1)
            toLabel.sizeToFit()
            toTextField.leftView = toLabel
            toTextField.leftViewMode = .always
        }
    }
    
    private var state: State? {
        didSet {
            state?.delegate = self
        }
    }
    
    func didGenerateAnnotations(_ annotations: [MKAnnotation]) {
        guard annotations.count > 0 else { return }
        mapView.addAnnotations(annotations)
        mapView.showAnnotations(state!.annotations, animated: true)
    }
    
    func didGenerateOverlays(_ overlays: [MKOverlay]) {
        guard overlays.count > 0 else { return }
        mapView.addOverlays(overlays, level: .aboveRoads)
        let rect = overlays.dropFirst().reduce(overlays[0].boundingMapRect) { MKMapRectUnion($0, $1.boundingMapRect) }
        mapView.setVisibleMapRect(rect, edgePadding: UIEdgeInsets(top: 150, left: 20, bottom: 20, right: 20), animated: true)
    }
    
    @IBAction func fromTextFieldDone() {
        toTextField.becomeFirstResponder()
    }
    
    @IBAction func toTextFieldDone() {
        toTextField.resignFirstResponder()
        guard let to = toTextField.text, !to.isEmpty else {
            let alert = UIAlertController(title: "Please enter a destination", message: nil, preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "OK", style: .default))
            present(alert, animated: true)
            return
        }
        let from: Location
        if let text = fromTextField.text, !text.isEmpty {
            from = .name(text)
        } else {
            guard let location = mapView.userLocation.location else {
                let alert = UIAlertController(title: "Please enter an origination", message: "Cannot get current location.", preferredStyle: .alert)
                alert.addAction(UIAlertAction(title: "OK", style: .default))
                present(alert, animated: true)
                return
            }
            from = .coordinate(location.coordinate)
        }
        if let annotations = state?.annotations, !annotations.isEmpty {
            mapView.removeAnnotations(annotations)
        }
        if let overlays = state?.overlays, !overlays.isEmpty {
            mapView.removeOverlays(overlays)
        }
        state = RoutingState(from: from, to: .name(to))
    }
    
    func mapView(_ mapView: MKMapView, rendererFor overlay: MKOverlay) -> MKOverlayRenderer {
        if let polyline = overlay as? MKPolyline {
            let renderer = MKPolylineRenderer(polyline: polyline)
            renderer.strokeColor = .blue
            renderer.lineWidth = 1
            return renderer
        }
        if let tile = overlay as? MKTileOverlay {
            return MKTileOverlayRenderer(tileOverlay: tile)
        }
        return MKOverlayRenderer(overlay: overlay)
    }
    
    func mapView(_ mapView: MKMapView, regionDidChangeAnimated animated: Bool) {
        if mapView.camera.altitude < 1265.4 {
            let camera = mapView.camera
            let newCamera = MKMapCamera(lookingAtCenter: camera.centerCoordinate, fromDistance: 1265.5, pitch: camera.pitch, heading: camera.heading)
            mapView.setCamera(newCamera, animated: true)
        }
    }
}
