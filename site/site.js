(() => {
  const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)');
  const stage = document.querySelector('[data-eye-stage]');
  const eyes = document.querySelector('[data-eyes]');

  if (stage && eyes && !reduceMotion.matches) {
    let x = 0;
    let y = 0;
    let targetX = 0;
    let targetY = 0;
    let frame = 0;

    const draw = () => {
      x += (targetX - x) * 0.09;
      y += (targetY - y) * 0.09;
      eyes.style.transform = `translate3d(${x}px, ${y}px, 0)`;
      frame = requestAnimationFrame(draw);
    };

    const point = (event) => {
      const rect = stage.getBoundingClientRect();
      const nx = ((event.clientX - rect.left) / rect.width) * 2 - 1;
      const ny = ((event.clientY - rect.top) / rect.height) * 2 - 1;
      targetX = Math.max(-1, Math.min(1, nx)) * 9;
      targetY = Math.max(-1, Math.min(1, ny)) * 6;
    };

    const centre = () => {
      targetX = 0;
      targetY = 0;
    };

    stage.addEventListener('pointermove', point, { passive: true });
    stage.addEventListener('pointerleave', centre, { passive: true });
    frame = requestAnimationFrame(draw);

    reduceMotion.addEventListener('change', (event) => {
      if (event.matches) {
        cancelAnimationFrame(frame);
        eyes.style.transform = '';
      }
    }, { once: true });
  }

  if (!reduceMotion.matches && 'IntersectionObserver' in window) {
    document.documentElement.classList.add('has-reveal');
    const reveal = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add('is-visible');
          reveal.unobserve(entry.target);
        }
      });
    }, { rootMargin: '0px 0px -6% 0px', threshold: 0.08 });

    document.querySelectorAll('.section, .media-stage, .privacy-band, .workshop').forEach((el) => reveal.observe(el));
  }
})();
