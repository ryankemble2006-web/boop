"""Non-visual material integration and preserved-art/motion contracts."""
from pathlib import Path
import subprocess
import unittest

ROOT = Path(__file__).resolve().parents[1]
BASE = "f64d49b39bd292ffa08011fcec8c61a50113f426"
SHADER = ROOT / "unified/animation/assets/eyes.frag"

class FeltLidContract(unittest.TestCase):
    def test_open_neutral_pose_does_not_bypass_new_lid_material(self):
        shader = SHADER.read_text()
        self.assertNotIn("dot(abs(uPose)", shader,
            "The neutral open pose returns the digital bitmap before material shading")

    def test_glsl_es_vertex_fragment_link(self):
        subprocess.run(["glslangValidator", "-l",
            str(ROOT / "unified/animation/assets/eyes.vert"), str(SHADER)], check=True)

    def test_only_lid_shader_and_version_change_in_production(self):
        changed = subprocess.check_output([
            "git", "diff", "--name-only", BASE, "HEAD", "--",
            "source", "unified", "scripts", "launcher", "shield-overlay"
        ], cwd=ROOT, text=True).splitlines()
        self.assertLessEqual(set(changed), {
            "unified/animation/assets/eyes.frag", "unified/app-build.gradle"
        })

    def test_original_art_and_motion_are_unchanged(self):
        paths = [
            "unified/assets/boop-eyes/boopApprovedEyes.png",
            "unified/assets/boop-notifications/boop-yellow-hands-approved.png",
            "unified/animation/java/com/boop/eyes/EyeMotion.java",
            "unified/animation/java/com/boop/eyes/EyeCatalogue.java",
            "unified/animation/java/com/boop/eyes/AnimationClock.java",
            "unified/animation/java/com/boop/eyes/ProductionAnimationController.java",
            "unified/animation/java/com/boop/eyes/SignMotion.java",
            "unified/animation/java/com/boop/eyes/CanonicalEyeRenderer.java",
            "source/BoopSharedEyeColourRuntime.java",
            "unified/animation/assets/lid-rig.png",
        ]
        for path in paths:
            with self.subTest(path=path):
                expected = subprocess.check_output(["git", "show", BASE + ":" + path], cwd=ROOT)
                self.assertEqual(expected, (ROOT / path).read_bytes())

    def test_gaze_hue_and_alpha_contracts_retained(self):
        shader = SHADER.read_text()
        baseline = subprocess.check_output(["git", "show", BASE + ":unified/animation/assets/eyes.frag"], cwd=ROOT).decode()
        # Material must not replace the gaze sampling, iris mask or premultiplied output.
        for start, end in [("vec3 hueRotate", "void main()")]:
            self.assertEqual(baseline.split(start)[1].split(end)[0],
                             shader.split(start)[1].split(end)[0])
        for line in [
            "vec2 source=p-uPose.zw*vec2(36.0,25.0)*support;",
            "rgb=mix(rgb,hueRotate(rgb,uHueRadians),irisMask(source));",
            "gl_FragColor=vec4(rgb*original.a,original.a);",
        ]:
            self.assertIn(line, shader)

if __name__ == "__main__":
    unittest.main()
