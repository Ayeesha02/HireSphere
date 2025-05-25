import React from 'react';
import { Link } from 'react-router-dom';
import {RiMapPin2Fill,RiPhoneFill } from 'react-icons/ri'; 


  const Footer = () => {
  

    return (

<footer className="footer">
<div className="section__container footer__container">
  <div className="footer__col">
    <div className="footer__logo">
      <Link to="/" className="logo">Hire<span>Sphere</span></Link> {/* Updated to Link */}
    </div>
    <p>
      Our platform is designed to help you find the perfect job and
      achieve your professional dreams.
    </p>
  </div>
  <div className="footer__col">
    <h4>Quick Links</h4>
    <ul className="footer__links">
      <li><Link to="/">Home</Link></li> {/* Updated to Link */}
      <li><a href="#about">About Us</a></li> {/* Kept as anchor */}
      <li><a href="#job">Jobs</a></li> {/* Kept as anchor */}
      <li><a href="#client">Testimonials</a></li> {/* Kept as anchor */}
      <li><a href="#contactUs">Contact Us</a></li> {/* Updated to button */}
      <li><Link to="/privacy">Privacy Policy</Link></li>
    </ul>
  </div>
  <div className="footer__col">
    <h4>Follow Us</h4>
    <ul className="footer__links">
      <li><button className="footer__link-btn">Facebook</button></li> {/* Updated to button */}
      <li><button className="footer__link-btn">Instagram</button></li> {/* Updated to button */}
      <li><button className="footer__link-btn">LinkedIn</button></li> {/* Updated to button */}
      <li><button className="footer__link-btn">Twitter</button></li> {/* Updated to button */}
      <li><button className="footer__link-btn">Youtube</button></li> {/* Updated to button */}
    </ul>
  </div>
  <div className="footer__col">
    <h4>Contact Us</h4>
    <ul className="footer__links">
      <li>
        <button className="footer__link-btn">
          <span><RiPhoneFill /></span> +91 234 5678
        </button> {/* Updated to button */}
      </li>
      <li>
        <button className="footer__link-btn">
          <span><RiMapPin2Fill /></span> Address...........
        </button> {/* Updated to button */}
      </li>
    </ul>
  </div>
</div>
<div className="footer__bar"></div>
</footer>
 );
};

export default Footer;