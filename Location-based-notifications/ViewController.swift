import UIKit
import UserNotifications
import CoreLocation

class ViewController: UIViewController, CLLocationManagerDelegate {
    
    var isGrantedNotificationAccess:Bool = false
    
    let locationManager = CLLocationManager()

    override func viewDidLoad() {
        //Performs any additional setup after loading the view
        super.viewDidLoad()
        
        let bttn = UIButton(frame: CGRect(x: 100, y: 200, width: 100, height: 50))
        bttn.setTitle("Schedule", for: .normal)
        bttn.setTitleColor(UIColor.gray, for: .normal)
        bttn.addTarget(self, action: #selector(ViewController.scheduleNotification), for: .touchUpInside)
        view.addSubview(bttn)
        
        setUpNotification()
        setUpLocationManager()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        //Condition when status is not determined
        if CLLocationManager.authorizationStatus() == .notDetermined {
            locationManager.requestAlwaysAuthorization()
        }
        //Condition when authorization is denied 
        else if CLLocationManager.authorizationStatus() == .denied {
            showAlert(message: "Location services have been denied. Please enable location services for Survival in Settings.")
        }
        //Condition when authorization is successful
        else if CLLocationManager.authorizationStatus() == .authorizedAlways {
            locationManager.startUpdatingLocation()
        }
    }
    //Refer to super for function
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }
    
    func setUpNotification() -> Void {
        //Shows the user an alert the first time they use the Survival app
        let options: UNAuthorizationOptions = [.alert, .sound]
        //Make authorization request using shared notification center
        UNUserNotificationCenter.current().requestAuthorization(options: options) {
            (granted, error) in
            self.isGrantedNotificationAccess = granted
        }
    }
    
    func scheduleNotification(trigger: UNNotificationTrigger) -> Void {
        if isGrantedNotificationAccess {
            //Display notification content
            let content = UNMutableNotificationContent()
            content.title = "Warning:"
            content.body = "Entering a high crime zone area"
            content.sound = UNNotificationSound.default()
            
            //Notification Trigger (if needed)
            //let trigger = UNTimeIntervalNotificationTrigger(timeInterval: 10,
            //                                                repeats: false)
            
            //Scheduling
            let identifier = "TestNotification"
            let request = UNNotificationRequest(identifier: identifier,
                                                content: content, trigger: trigger)
            UNUserNotificationCenter.current().add(request, withCompletionHandler: { (error) in
                if let error = error {
                    //Error if something goes wrong
                    print(error)
                }
            })
        }
    }
    
    func setUpLocationManager() -> Void {
        //Sets up locationManager
        locationManager.delegate = self;
        locationManager.distanceFilter = kCLLocationAccuracyNearestTenMeters;
        locationManager.desiredAccuracy = kCLLocationAccuracyBest;
        
        if CLLocationManager.isMonitoringAvailable(for: CLCircularRegion.self) {
            
            // Region data
            let title = "Location"
            let coordinate = CLLocationCoordinate2DMake(90.0000, 0.0000)           
            let regionRadius = 300.0
            
            // Setup region
            let region = CLCircularRegion(center: CLLocationCoordinate2D(latitude: coordinate.latitude,
                                                                         longitude: coordinate.longitude), radius: regionRadius, identifier: title)
            locationManager.startMonitoring(for: region)
        }
    }
    
    func locationManager(_ manager: CLLocationManager, didEnterRegion region: CLRegion) {
        //showAlert(message: "enter \(region.identifier)")
        
        let trigger = UNLocationNotificationTrigger(region:region, repeats:false)
        scheduleNotification(trigger: trigger)
    }
    
    func locationManager(_ manager: CLLocationManager, didExitRegion region: CLRegion) {
        showAlert(message: "didExitRegion \(region.identifier)")
    }
    
    func showAlert(message: String) -> Void {
        let alert = UIAlertController(title: "Alert", message: message, preferredStyle: .alert)
        let bttn = UIAlertAction(title: "Ok", style: .default, handler: nil)
        alert.addAction(bttn)
        present(alert, animated: true, completion: nil)
        
    }

    

}

