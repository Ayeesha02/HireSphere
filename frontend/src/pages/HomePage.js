import React, { useState } from 'react';
import { Link } from 'react-router-dom'; 
import { Swiper, SwiperSlide } from 'swiper/react';
import 'swiper/swiper-bundle.css';
import { Navigation, Pagination } from 'swiper/modules'; 
import { RiMenuLine, RiCloseLine, RiPencilRuler2Fill, RiBarChartBoxFill, RiMegaphoneFill, RiWallet3Fill, RiCarFill, RiTruckFill, RiComputerFill, RiBuildingFill, RiPhoneFill, RiMapPin2Fill, RiUserFill, RiSearchFill, RiFilePaperFill, RiBriefcaseFill } from 'react-icons/ri';
import google from '../assets/google.png';
import twitter from '../assets/twitter.png';
import amazon from '../assets/amazon.png';
import figma from '../assets/figma.png';
import linkedin from '../assets/linkedin.png';
import microsoft from '../assets/microsoft.png';
import bag from '../assets/bag.png';
import offer1 from '../assets/Offer1.jpg';
import offer2 from '../assets/offer2.jpg';
import offer3 from '../assets/offer3.jpg';
import client1 from '../assets/Client1.jpg';
import client2 from '../assets/cilent2.jpg'; 
import client3 from '../assets/client3.jpg';

const HomePage = () => {
  const [isOpen, setIsOpen] = useState(false);

  const toggleMenu = () => {
    setIsOpen(!isOpen);
  };

  return (
    <div>
      <nav>
        <div className="nav__header">
          <div className="nav__logo">
            <Link to="/" className="logo">Hire<span>Sphere</span></Link>
          </div>
          <div className="nav__menu__btn" id="menu-btn" onClick={toggleMenu}>
            {isOpen ? <RiCloseLine /> : <RiMenuLine />}
          </div>
        </div>
        <ul className={`nav__links ${isOpen ? 'open' : ''}`} id="nav-links">
          <li><Link to="/">Home</Link></li>
          <li><a href="#about">About</a></li> 
          <li><a href="#job">Jobs</a></li> 
          <li><a href="#service">Services</a></li> 
          <li><a href="#client">Client</a></li> 
          <li><Link to="/Login">Login</Link></li> 
          <li><Link to="/SignUp" className="btn">Register</Link></li> 
        </ul>
      </nav>
      <header className="header">
        <div className="section__container header__container" id="home">
          <img src={google} alt="Google icon" />
          <img src={twitter} alt="Twitter icon" />
          <img src={amazon} alt="Amazon icon" />
          <img src={figma} alt="Figma icon" />
          <img src={linkedin} alt="Linkedin icon" />
          <img src={microsoft} alt="Microsoft icon" />
          <h2>
            <img src={bag} alt="bag" />
            Find Your Dream Job
          </h2>
          <h1>Search, Answer, Apply &<br />Get Your <span>Job</span></h1>
          <p>
            Embark on your journey today. Explore endless opportunities, take charge by applying for roles that align with your skills and ambitions, and shape a brighter future for your career with the help of AI.
          </p>
          <div className="header__btns">
          <li><Link to="/SignUp" className="Browse_job_btn">Browse Jobs</Link></li>  
          </div>
        </div>
      </header>
      <section className="steps" id="about">
        <div className="section__container steps__container">
          <h2 className="section__header">
            Get Hired <span>Using Us</span>
          </h2>
          <p className="section__description">
            Follow these simple steps and land your dream job.
          </p>
          <div className="steps__grid">
            <div className="steps__card">
              <span><RiUserFill /></span>
              <h4>Create an Account</h4>
              <p>
                Sign up in just a few easy steps to gain exclusive access to a world of job opportunities and take the first step towards landing your dream job. It's fast, simple, and completely free.
              </p>
            </div>
            <div className="steps__card">
              <span><RiSearchFill /></span>
              <h4>Search Job</h4>
              <p>
                Explore our job database designed to align with your skills and preferences. With our advanced search filters, discovering the ideal job has never been simpler.
              </p>
            </div>
            <div className="steps__card">
              <span><RiFilePaperFill /></span>
              <h4>Upload CV & Get Interviewed</h4>
              <p>
                Highlight your experience by uploading your CV and get interviewed by our AI chatbot, and show employers why you're the ideal candidate for their job opportunities.
              </p>
            </div>
            <div className="steps__card">
              <span><RiBriefcaseFill /></span>
              <h4>Get Job</h4>
              <p>
                Take the last step toward your new career. Prepare to begin your professional journey and land the job you've always dreamed of.
              </p>
            </div>
          </div>
        </div>
      </section>
      <section className="section__container explore__container">
        <h2 className="section__header">
          <span>Countless Career Options</span> Are Waiting For You To Explore
        </h2>
        <p className="section__description">
          Discover a World of Exciting Opportunities and Endless Possibilities,
          and Find the Perfect Career Path to Shape Your Future.
        </p>
        <div className="explore__grid">
          <div className="explore__card">
            <span><RiPencilRuler2Fill /></span>
            <h4>Computer Science</h4>
            <p>100+ jobs openings</p>
          </div>
          <div className="explore__card">
            <span><RiBarChartBoxFill /></span>
            <h4>Sales</h4>
            <p>300+ jobs openings</p>
          </div>
          <div className="explore__card">
            <span><RiMegaphoneFill /></span>
            <h4>Marketing</h4>
            <p>520+ jobs openings</p>
          </div>
          <div className="explore__card">
            <span><RiWallet3Fill /></span>
            <h4>Finance</h4>
            <p>100+ jobs openings</p>
          </div>
          <div className="explore__card">
            <span><RiCarFill /></span>
            <h4>Automobile</h4>
            <p>200+ jobs openings</p>
          </div>
          <div className="explore__card">
            <span><RiTruckFill /></span>
            <h4>Architecture</h4>
            <p>1k+ jobs openings</p>
          </div>
          <div className="explore__card">
            <span><RiComputerFill /></span>
            <h4>Design</h4>
            <p>100+ jobs openings</p>
          </div>
          <div className="explore__card">
            <span><RiBuildingFill /></span>
            <h4>Construction</h4>
            <p>500+ jobs openings</p>
          </div>
        </div>
        <div className="explore__btn">
          <button className="btn">View All Categories</button> 
        </div>
      </section>
      <section className="section__container job__container" id="job">
        <h2 className="section__header"><span>Latest & Top</span> Job Openings</h2>
        <p className="section__description">
          Discover Exciting New Opportunities and High-Demand Positions Available
          Now in Top Industries and Companies
        </p>
        <div className="job__grid">
          <div className="job__card">
            <div className="job__card__header">
              <img src={figma} alt="job" />
              <div>
                <h5>Figma</h5>
                <h6>Australia</h6>
              </div>
            </div>
            <h4>Senior Product Engineer</h4>
            <p>
              Lead the development of innovative product solutions, leveraging
              your expertise in engineering and product management to drive
              success.
            </p>
            <div className="job__card__footer">
              <span>12 Positions</span>
              <span>Full Time</span>
              <span>$1,45,000/Year</span>
            </div>
          </div>
          <div className="job__card">
            <div className="job__card__header">
              <img src={google} alt="job" />
              <div>
                <h5>Google</h5>
                <h6>USA</h6>
              </div>
            </div>
            <h4>Project Manager</h4>
            <p>
              Manage project timelines and budgets to ensure successful delivery
              of projects on schedule, while maintaining clear communication with
              stakeholders.
            </p>
            <div className="job__card__footer">
              <span>2 Positions</span>
              <span>Full Time</span>
              <span>$95,000/Year</span>
            </div>
          </div>
          <div className="job__card">
            <div className="job__card__header">
              <img src={linkedin} alt="job" />
              <div>
                <h5>LinkedIn</h5>
                <h6>Germany</h6>
              </div>
            </div>
            <h4>Full Stack Developer</h4>
            <p>
              Develop and maintain both front-end and back-end components of web
              applications, utilizing a wide range of programming languages and
              frameworks.
            </p>
            <div className="job__card__footer">
              <span>10 Positions</span>
              <span>Full Time</span>
              <span>$35,000/Year</span>
            </div>
          </div>
          <div className="job__card">
            <div className="job__card__header">
              <img src={amazon} alt="job" />
              <div>
                <h5>Amazon</h5>
                <h6>USA</h6>
              </div>
            </div>
            <h4>Front-end Developer</h4>
            <p>
              Design and implement user interfaces using HTML, CSS, and
              JavaScript, collaborating closely with designers and back-end
              developers.
            </p>
            <div className="job__card__footer">
              <span>20 Positions</span>
              <span>Full Time</span>
              <span>$1,01,000/Year</span>
            </div>
          </div>
          <div className="job__card">
            <div className="job__card__header">
              <img src={twitter} alt="job" />
              <div>
                <h5>Twitter</h5>
                <h6>USA</h6>
              </div>
            </div>
            <h4>ReactJS Developer</h4>
            <p>
              Specialize in building dynamic and interactive user interfaces using
              the ReactJS library, leveraging your expertise in JavaScript and
              front-end development.
            </p>
            <div className="job__card__footer">
              <span>6 Positions</span>
              <span>Full Time</span>
              <span>$98,000/Year</span>
            </div>
          </div>
          <div className="job__card">
            <div className="job__card__header">
              <img src={microsoft} alt="job" />
              <div>
                <h5>Microsoft</h5>
                <h6>USA</h6>
              </div>
            </div>
            <h4>Python Developer</h4>
            <p>
              Develop scalable and efficient backend systems and applications
              using Python, utilizing your proficiency in Python programming and
              software development.
            </p>
            <div className="job__card__footer">
              <span>9 Positions</span>
              <span>Full Time</span>
              <span>$80,000/Year</span>
            </div>
          </div>
        </div>
      </section>
      <section className="section__container offer__container" id="service">
        <h2 className="section__header">What We <span>Offer</span></h2>
        <p className="section__description">
          Explore the Benefits and Services We Provide to Enhance Your Job Search
          and Career Success
        </p>
        <div className="offer__grid">
          <div className="offer__card">
            <img src={offer1} alt="offer" />
            <div className="offer__details">
              <span>01</span>
              <div>
                <h4>Job Recommendation</h4>
                <p>Personalized job matches tailored to your skills and preferences</p>
              </div>
            </div>
          </div>
          <div className="offer__card">
            <img src={offer2} alt="offer" />
            <div className="offer__details">
              <span>02</span>
              <div>
                <h4>Ease Assessment of Skills</h4>
                <p>Showcase your skills to the employer in the earlier stage</p>
              </div>
            </div>
          </div>
          <div className="offer__card">
            <img src={offer3} alt="offer" />
            <div className="offer__details">
              <span>03</span>
              <div>
                <h4>Job Offers</h4>
                <p>Apply for jobs as soon as posted by the employer</p>
              </div>
            </div>
          </div>
        </div>
      </section>
      <section className="section__container client__container" id="client">
        <h2 className="section__header">What Our <span>Client Say</span></h2>
        <p className="section__description">
          Read Testimonials and Success Stories from Our Satisfied Job Seekers and
          Employers to See How We Make a Difference
        </p>
        <Swiper
          modules={[Navigation, Pagination]} 
          spaceBetween={30}
          slidesPerView={1}
          navigation
          pagination={{ clickable: true }}
        >
          <SwiperSlide>
            <div className="client__card">
              <img src={client1} alt="client" />
              <p>
                Searching for a job can be overwhelming, but this platform made
                it simple and efficient. I uploaded my resume, applied to a few
                positions, and soon enough, I was hired! Thank you for helping
                me kickstart my career!
              </p>
              <div className="client__ratings">
                <span><i className="ri-star-fill"></i></span>
                <span><i className="ri-star-fill"></i></span>
                <span><i className="ri-star-fill"></i></span>
                <span><i className="ri-star-fill"></i></span>
                <span><i className="ri-star-fill"></i></span>
              </div>
              <h4>Aasimah Bahu</h4>
              <h5>Graphic Designer</h5>
            </div>
          </SwiperSlide>
          <SwiperSlide>
            <div className="client__card">
              <img src={client3} alt="client" />
              <p>
                As a recent graduate, I was unsure where to start my job search.
                This website guided me through the process step by step. From
                creating my profile to receiving job offers, everything was
                seamless. I'm now happily employed thanks to this platform!
              </p>
              <div className="client__ratings">
                <span><i className="ri-star-fill"></i></span>
                <span><i className="ri-star-fill"></i></span>
                <span><i className="ri-star-fill"></i></span>
                <span><i className="ri-star-fill"></i></span>
                <span><i className="ri-star-half-fill"></i></span>
              </div>
              <h4>Felix Lee</h4>
              <h5>Recent Graduate</h5>
            </div>
          </SwiperSlide>
          <SwiperSlide>
            <div className="client__card">
              <img src={client2} alt="client" />
              <p>
                Creating an account was a breeze, and I was amazed by the number
                of job opportunities available. Thanks to this website, I found
                the perfect position that aligned perfectly with my career
                goals.
              </p>
              <div className="client__ratings">
                <span><i className="ri-star-fill"></i></span>
                <span><i className="ri-star-fill"></i></span>
                <span><i className="ri-star-fill"></i></span>
                <span><i className="ri-star-fill"></i></span>
                <span><i className="ri-star-fill"></i></span>
              </div>
              <h4>Christopher Bang</h4>
              <h5>Software Engineer</h5>
            </div>
          </SwiperSlide>
        </Swiper>
      </section>
      <footer className="footer">
        <div className="section__container footer__container">
          <div className="footer__col">
            <div className="footer__logo">
              <Link to="/" className="logo">Hire<span>Sphere</span></Link> 
            </div>
            <p>
              Our platform is designed to help you find the perfect job and
              achieve your professional dreams.
            </p>
          </div>
          <div className="footer__col">
            <h4>Quick Links</h4>
            <ul className="footer__links">
              <li><Link to="/">Home</Link></li>
              <li><a href="#about">About Us</a></li> 
              <li><a href="#job">Jobs</a></li> 
              <li><a href="#client">Testimonials</a></li> 
              <li><a href="#contactUs">Contact Us</a></li> 
              <li><Link to="/privacy">Privacy</Link></li>
            </ul>
          </div>
          <div className="footer__col">
            <h4>Follow Us</h4>
            <ul className="footer__links">
              <li><button className="footer__link-btn">Facebook</button></li> 
              <li><button className="footer__link-btn">Instagram</button></li>
              <li><button className="footer__link-btn">LinkedIn</button></li> 
              <li><button className="footer__link-btn">Twitter</button></li>
              <li><button className="footer__link-btn">Youtube</button></li> 
            </ul>
          </div>
          <div className="footer__col">
            <h4>Contact Us</h4>
            <ul className="footer__links">
              <li>
                <button className="footer__link-btn">
                  <span><RiPhoneFill /></span> +971 234 5678
                </button> 
              </li>
              <li>
                <button className="footer__link-btn">
                  <span><RiMapPin2Fill /></span> Address...........
                </button>
              </li>
            </ul>
          </div>
        </div>
        <div className="footer__bar"></div>
      </footer>
    </div>
  );
};

export default HomePage;