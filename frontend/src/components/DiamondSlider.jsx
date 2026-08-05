import { useEffect, useState } from "react";
import image1 from "../images/11.png";
import image2 from "../images/22.png";
import image3 from "../images/33.png";

const images = [image1, image2, image3];

export default function DiamondSlider() {
    const [index, setIndex] = useState(0);

    useEffect(() => {
        const timer = setInterval(() => {
            setIndex(prev => (prev + 1) % images.length);
        }, 4000);
        return () => clearInterval(timer);
    }, []);

    return (
        <div className="diamond">
            <img src={images[index]} alt="" />
        </div>
    );
}